package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.IGroupLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.ILayerList;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.CustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapImage;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.view.components.UI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages undo and redo operations for map objects. This class provides functionality to track changes to map objects and allows undoing and redoing
 * those changes.
 */
public class UndoManager {
  private static final Logger log = Logger.getLogger(UndoManager.class.getName());
  private static final int MAX_STACK_SIZE = 10000;
  private int nextOperation = 1;
  private final UndoState[] undoStack;
  private final List<IMapObject> changing;
  private int currentIndex = -1;
  private final IMap map;
  private int operation = 0;
  private int savedIndex = -1;
  private boolean untrackedChanges;
  private boolean executing;
  private long revision;
  private int eventAggregationDepth;
  private boolean undoStackChangedPending;
  private ILayer changingLayer;
  private Map<String, Object> changingLayerSnapshot;
  private IMap changingMap;
  private Map<String, Object> changingMapSnapshot;
  private IMap changingLayerStructureMap;
  private List<LayerNode> changingLayerStructureSnapshot;
  private IMap changingLayersMap;
  private List<LayerPropertiesNode> changingLayersSnapshot;

  private static final Map<IMap, UndoManager> instance;
  private static final List<Consumer<UndoManager>> undoStackChangedConsumers;
  private static final List<Consumer<UndoManager>> mapObjectAdded;
  private static final List<Consumer<UndoManager>> mapObjectRemoved;

  private UndoManager(IMap map) {
    this.changing = new CopyOnWriteArrayList<>();
    this.undoStack = new UndoState[MAX_STACK_SIZE];
    this.map = map;
  }

  static {
    instance = new IdentityHashMap<>();
    undoStackChangedConsumers = new CopyOnWriteArrayList<>();
    mapObjectAdded = new CopyOnWriteArrayList<>();
    mapObjectRemoved = new CopyOnWriteArrayList<>();
  }

  /**
   * Gets the instance of the UndoManager for the current map. If an instance does not exist for the current map, a new one is created.
   *
   * @return The UndoManager instance for the current map.
   */
  public static UndoManager instance() {
    IMap map = Game.world().environment().getMap();
    return forMap(map);
  }

  public static UndoManager forMap(IMap map) {
    if (map == null) {
      throw new IllegalArgumentException("Map must not be null.");
    }
    return instance.computeIfAbsent(map, UndoManager::new);
  }

  public static void remove(IMap map) {
    instance.remove(map);
  }

  /**
   * Clears all instances of the UndoManager.
   */
  public static void clearAll() {
    instance.clear();
  }

  /**
   * Begins a new operation by setting the current operation to the next operation identifier.
   */
  public void beginOperation() {
    if (this.operation != 0) {
      return;
    }
    this.operation = nextOperation;
    this.beginEventAggregation();
  }

  public boolean tryBeginOperation() {
    if (this.operation != 0) {
      return false;
    }
    this.beginOperation();
    return true;
  }

  /**
   * Ends the current operation by incrementing the next operation identifier and resetting the current operation.
   */
  public void endOperation() {
    if (this.operation == 0) {
      return;
    }
    ++nextOperation;
    this.operation = 0;
    this.endEventAggregation();
  }

  /**
   * Undoes the last operation. If there are no operations to undo or if an operation is currently being executed, this method returns immediately.
   */
  public void undo() {
    if (this.executing || this.currentIndex < 0) {
      return;
    }

    final int currentOperation = this.undoStack[this.currentIndex].getOperation();
    if (!canRestoreMapNames(this.currentIndex, -1, currentOperation, true)) {
      return;
    }

    int stepsUndone = 0;
    this.executing = true;
    try {
      List<IMapObject> affectedTargets = new ArrayList<>();
      ILayer affectedLayer = null;
      boolean affectedMap = false;
      boolean affectedLayerStructure = false;
      do {
        stepsUndone++;
        final UndoState state = this.undoStack[this.currentIndex];
        if (state.target != null && affectedTargets.stream().noneMatch(m -> m.getId() == state.target.getId())) {
          affectedTargets.add(state.target);
        }
        if (state.targetLayer != null) {
          affectedLayer = state.targetLayer;
        }
        if (state.targetMap != null) {
          affectedMap = true;
        }
        if (state.targetLayerStructureMap != null) {
          affectedLayerStructure = true;
        }

        switch (state.operationType) {
          case ADD -> Editor.instance().getMapComponent().delete(state.target);
          case CHANGE, MOVE, RESIZE -> restoreState(state.target, Objects.requireNonNull(state.oldMapObject));
          case DELETE -> Editor.instance().getMapComponent().add(state.target, state.layer);
          case LAYER_CHANGE -> restoreLayerProperties(state.targetLayer, state.oldLayerProperties);
          case LAYER_TREE_CHANGE -> restoreLayerProperties(state.oldLayerTreeProperties);
          case MAP_CHANGE -> restoreMapProperties(state.targetMap, state.oldMapProperties);
          case LAYER_STRUCTURE_CHANGE -> restoreLayerStructure(state.targetLayerStructureMap, state.oldLayerStructure);
          case RESOURCE_CHANGE -> state.undoResourceAction.run();
        }

        this.currentIndex--;
      } while (currentOperation != 0
        && this.currentIndex >= 0
        && this.undoStack[this.currentIndex].getOperation() == currentOperation);

      log.log(Level.FINE, "{0} steps undone.", stepsUndone);
      if (isCurrentMap()) {
        refreshAffectedTargets(affectedTargets);
        if (affectedLayer != null || affectedMap || affectedLayerStructure) {
          refreshLayerAndMapViews(affectedLayer, affectedMap);
        }
      }
      fireUndoStackChangedEvent(this);
    } finally {
      this.executing = false;
    }
  }

  /**
   * Undoes up to the specified number of operations and emits one stack-change event.
   *
   * @param operations The number of operations to undo.
   */
  public void undo(int operations) {
    if (operations <= 0) {
      return;
    }

    this.beginEventAggregation();
    try {
      for (int operationIndex = 0; operationIndex < operations && this.canUndo(); operationIndex++) {
        this.undo();
      }
    } finally {
      this.endEventAggregation();
    }
  }

  /**
   * Redoes the last undone operation. If there are no operations to redo or if an operation is currently being executed, this method returns
   * immediately.
   */
  public void redo() {
    if (this.executing
      || this.undoStack.length - 1 == this.currentIndex
      || this.undoStack[this.currentIndex + 1] == null) {
      return;
    }

    if (this.currentIndex >= this.undoStack.length - 1) {
      this.currentIndex = this.undoStack.length - 1;
      return;
    }

    final int currentOperation = this.undoStack[this.currentIndex + 1].getOperation();
    if (!canRestoreMapNames(this.currentIndex + 1, 1, currentOperation, false)) {
      return;
    }

    int stepsRedone = 0;
    this.executing = true;
    try {
      List<IMapObject> affectedTargets = new ArrayList<>();
      ILayer affectedLayer = null;
      boolean affectedMap = false;
      boolean affectedLayerStructure = false;
      do {
        ++stepsRedone;
        ++this.currentIndex;

        final UndoState state = this.undoStack[this.currentIndex];
        if (state.target != null && affectedTargets.stream().noneMatch(m -> m.getId() == state.target.getId())) {
          affectedTargets.add(state.target);
        }
        if (state.targetLayer != null) {
          affectedLayer = state.targetLayer;
        }
        if (state.targetMap != null) {
          affectedMap = true;
        }
        if (state.targetLayerStructureMap != null) {
          affectedLayerStructure = true;
        }

        switch (state.operationType) {
          case ADD -> Editor.instance().getMapComponent().add(state.target, state.layer);
          case CHANGE, MOVE, RESIZE -> restoreState(state.target, Objects.requireNonNull(state.newMapObject));
          case DELETE -> Editor.instance().getMapComponent().delete(state.target);
          case LAYER_CHANGE -> restoreLayerProperties(state.targetLayer, state.newLayerProperties);
          case LAYER_TREE_CHANGE -> restoreLayerProperties(state.newLayerTreeProperties);
          case MAP_CHANGE -> restoreMapProperties(state.targetMap, state.newMapProperties);
          case LAYER_STRUCTURE_CHANGE -> restoreLayerStructure(state.targetLayerStructureMap, state.newLayerStructure);
          case RESOURCE_CHANGE -> state.redoResourceAction.run();
        }
      } while (currentOperation != 0
        && this.currentIndex < MAX_STACK_SIZE - 1
        && this.undoStack[this.currentIndex + 1] != null
        && this.undoStack[this.currentIndex + 1].getOperation() == currentOperation);

      log.log(Level.FINE, "{0} steps redone.", stepsRedone);

      if (isCurrentMap()) {
        refreshAffectedTargets(affectedTargets);
        if (affectedLayer != null || affectedMap || affectedLayerStructure) {
          refreshLayerAndMapViews(affectedLayer, affectedMap);
        }
      }
      fireUndoStackChangedEvent(this);
    } finally {
      this.executing = false;
    }
  }

  /**
   * Checks if an undo operation can be performed.
   *
   * @return True if an undo operation can be performed, false otherwise.
   */
  public boolean canUndo() {
    return this.currentIndex >= 0;
  }

  /**
   * Checks if a redo operation can be performed.
   *
   * @return True if a redo operation can be performed, false otherwise.
   */
  public boolean canRedo() {
    return this.currentIndex < MAX_STACK_SIZE - 1 && this.undoStack[this.currentIndex + 1] != null;
  }

  /**
   * Gets the undo stack.
   *
   * @return An array of UndoState objects representing the undo stack.
   */
  public UndoState[] getUndoStack() {
    return this.undoStack;
  }

  public long getRevision() {
    return this.revision;
  }

  public boolean undoIfRevision(long expectedRevision) {
    if (this.revision != expectedRevision || !this.canUndo()) {
      return false;
    }
    int previousIndex = this.currentIndex;
    this.undo();
    return this.currentIndex != previousIndex;
  }

  public List<HistoryEntry> getUndoHistory() {
    List<HistoryEntry> history = new ArrayList<>();
    for (int index = this.currentIndex; index >= 0; index--) {
      UndoState state = this.undoStack[index];
      int lastIndex = index;
      int steps = 1;
      if (state.operation != 0) {
        while (index - 1 >= 0 && this.undoStack[index - 1].operation == state.operation) {
          index--;
          steps++;
        }
      }
      history.add(new HistoryEntry(this.historyDescription(index, lastIndex, steps), steps));
    }
    return history;
  }

  public List<HistoryEntry> getRedoHistory() {
    List<HistoryEntry> history = new ArrayList<>();
    for (int index = this.currentIndex + 1; index < MAX_STACK_SIZE && this.undoStack[index] != null; index++) {
      UndoState state = this.undoStack[index];
      int firstIndex = index;
      int steps = 1;
      if (state.operation != 0) {
        while (index + 1 < MAX_STACK_SIZE
          && this.undoStack[index + 1] != null
          && this.undoStack[index + 1].operation == state.operation) {
          index++;
          steps++;
        }
      }
      history.add(new HistoryEntry(this.historyDescription(firstIndex, index, steps), steps));
    }
    return history;
  }

  public record HistoryEntry(String description, int steps) {
  }

  private String historyDescription(int firstIndex, int lastIndex, int steps) {
    UndoState firstState = this.undoStack[firstIndex];
    if (steps == 1) {
      return firstState.description();
    }

    Set<Integer> targetIds = new HashSet<>();
    for (int index = firstIndex; index <= lastIndex; index++) {
      UndoState state = this.undoStack[index];
      if (state.target == null || state.operationType != firstState.operationType) {
        return Resources.strings().get("history_groupedChanges", Integer.toString(steps));
      }
      targetIds.add(state.target.getId());
    }

    if (targetIds.size() <= 1) {
      return Resources.strings().get("history_groupedChanges", Integer.toString(steps));
    }

    String count = Integer.toString(targetIds.size());
    return switch (firstState.operationType) {
      case ADD -> Resources.strings().get("history_addObjects", count);
      case DELETE -> Resources.strings().get("history_deleteObjects", count);
      case MOVE -> Resources.strings().get("history_moveObjects", count);
      case RESIZE -> Resources.strings().get("history_resizeObjects", count);
      default -> Resources.strings().get("history_changeObjects", count);
    };
  }

  /**
   * Tracks the state of a map object before it is changed.
   *
   * @param mapObject The map object that is about to be changed.
   */
  public void mapObjectChanging(IMapObject mapObject) {
    if (executing || mapObject == null) {
      return;
    }

    if (this.changing.contains(mapObject)) {
      // the old state is already tracked, while multiple changes are carried
      // out, we
      // don't want to track the steps in between
      return;
    }

    this.changing.add(new MapObject((MapObject) mapObject, true));
  }

  /**
   * Updates the state of a map object after it has been changed.
   *
   * @param mapObject The map object that has been changed.
   */
  public void mapObjectChanged(IMapObject mapObject) {
    if (mapObject == null) {
      return;
    }

    this.mapObjectChanged(mapObject, mapObject.getId(), OperationType.CHANGE);
  }

  public void mapObjectMoved(IMapObject mapObject) {
    if (mapObject != null) {
      this.mapObjectChanged(mapObject, mapObject.getId(), OperationType.MOVE);
    }
  }

  /**
   * Redoes up to the specified number of operations and emits one stack-change event.
   *
   * @param operations The number of operations to redo.
   */
  public void redo(int operations) {
    if (operations <= 0) {
      return;
    }

    this.beginEventAggregation();
    try {
      for (int operationIndex = 0; operationIndex < operations && this.canRedo(); operationIndex++) {
        this.redo();
      }
    } finally {
      this.endEventAggregation();
    }
  }

  private boolean canRestoreMapNames(int start, int direction, int operation, boolean oldState) {
    for (int index = start; index >= 0 && index < MAX_STACK_SIZE; index += direction) {
      UndoState state = this.undoStack[index];
      if (state == null || index != start && (operation == 0 || state.operation != operation)) {
        break;
      }
      if (state.operationType == OperationType.MAP_CHANGE) {
        Map<String, Object> properties = oldState ? state.oldMapProperties : state.newMapProperties;
        if (!Editor.instance().getMapComponent().canRenameMap(
            state.targetMap, (String) properties.get("name"))) {
          log.log(Level.WARNING, "Cannot restore map name {0} because it is no longer unique.", properties.get("name"));
          return false;
        }
      }
      if (operation == 0) {
        break;
      }
    }
    return true;
  }

  private boolean isCurrentMap() {
    return Game.world().environment() != null && Game.world().environment().getMap() == this.map;
  }

  public void mapObjectResized(IMapObject mapObject) {
    if (mapObject != null) {
      this.mapObjectChanged(mapObject, mapObject.getId(), OperationType.RESIZE);
    }
  }

  /**
   * Updates the state of a map object after it has been changed.
   *
   * @param mapObject     The map object that has been changed.
   * @param previousMapId The previous ID of the map object before the change.
   */
  public void mapObjectChanged(IMapObject mapObject, int previousMapId) {
    this.mapObjectChanged(mapObject, previousMapId, OperationType.CHANGE);
  }

  private void mapObjectChanged(IMapObject mapObject, int previousMapId, OperationType operationType) {
    if (executing || mapObject == null) {
      return;
    }

    Optional<IMapObject> trackedMapObject =
      this.changing.stream().filter(x -> x.getId() == previousMapId).findFirst();
    if (trackedMapObject.isEmpty()) {
      // didn't track the changing event and therefore cannot provide an undo
      return;
    }

    this.ensureStackSize();

    this.currentIndex++;
    this.clearRedoSteps();

    this.undoStack[this.currentIndex] =
      new UndoState(
        mapObject,
        this.changing.remove(this.changing.indexOf(trackedMapObject.get())),
        new MapObject((MapObject) mapObject, true),
        operationType,
        this.operation);
    fireUndoStackChangedEvent(this);
  }

  /**
   * Deletes the specified map object and records the deletion in the undo stack.
   *
   * @param mapObject The map object to be deleted.
   */
  public void mapObjectDeleted(IMapObject mapObject) {
    if (executing || mapObject == null) {
      return;
    }

    this.ensureStackSize();
    this.currentIndex++;
    this.clearRedoSteps();

    this.undoStack[this.currentIndex] =
      new UndoState(mapObject, OperationType.DELETE, this.operation);
    fireUndoStackChangedEvent(this);
    fireUndoManagerEvent(mapObjectRemoved, this);
  }

  /**
   * Adds the specified map object and records the addition in the undo stack.
   *
   * @param mapObject The map object to be added.
   */
  public void mapObjectAdded(IMapObject mapObject) {
    if (executing || mapObject == null) {
      return;
    }

    this.ensureStackSize();
    this.currentIndex++;
    this.clearRedoSteps();

    this.undoStack[this.currentIndex] = new UndoState(mapObject, OperationType.ADD, this.operation);
    fireUndoStackChangedEvent(this);
    fireUndoManagerEvent(mapObjectAdded, this);
  }

  /**
   * This method is used to mark the current map as changed/unsaved which is mainly useful when something other than a {@code MapObject} changed (e.g.
   * a layer).
   */
  public void recordChanges() {
    this.untrackedChanges = true;
    fireUndoStackChangedEvent(this);
  }

  public void resourceChanged(Runnable undoAction, Runnable redoAction) {
    if (this.executing || undoAction == null || redoAction == null) {
      return;
    }
    this.ensureStackSize();
    this.currentIndex++;
    this.clearRedoSteps();
    this.undoStack[this.currentIndex] = new UndoState(undoAction, redoAction, this.operation);
    fireUndoStackChangedEvent(this);
  }

  private Map<String, Object> snapshotLayerProperties(ILayer layer) {
    Map<String, Object> props = new HashMap<>();
    props.put("name", layer.getName());
    props.put("opacity", layer.getOpacity());
    props.put("visible", layer.isVisible());
    props.put("tintColor", layer.getTintColor());
    props.put("renderType", layer.getRenderType());
    if (layer instanceof IMapObjectLayer mol) {
      props.put("color", mol.getColor());
    }
    if (layer instanceof IImageLayer imageLayer) {
      props.put("image", imageLayer.getImage() instanceof MapImage image ? new ImageSnapshot(image) : null);
    }
    for (Map.Entry<String, ICustomProperty> entry : layer.getProperties().entrySet()) {
      props.put("prop:" + entry.getKey(), new CustomProperty(entry.getValue()));
    }
    return props;
  }

  private static void restoreLayerProperties(ILayer layer, Map<String, Object> props) {
    layer.setName((String) props.get("name"));
    layer.setOpacity((float) props.get("opacity"));
    layer.setVisible((boolean) props.get("visible"));
    layer.setTintColor((java.awt.Color) props.get("tintColor"));
    layer.setRenderType((de.gurkenlabs.litiengine.graphics.RenderType) props.get("renderType"));
    if (layer instanceof IMapObjectLayer mol && props.containsKey("color")) {
      mol.setColor((java.awt.Color) props.get("color"));
    }
    if (layer instanceof de.gurkenlabs.litiengine.environment.tilemap.xml.ImageLayer imageLayer) {
      ImageSnapshot snapshot = (ImageSnapshot) props.get("image");
      imageLayer.setImage(snapshot != null ? snapshot.copy() : null);
    }
    layer.getProperties().keySet().removeIf(k -> true);
    for (Map.Entry<String, Object> entry : props.entrySet()) {
      if (entry.getKey().startsWith("prop:")) {
        layer.setValue(entry.getKey().substring(5), new CustomProperty((ICustomProperty) entry.getValue()));
      }
    }
  }

  private Map<String, Object> snapshotMapProperties(IMap map) {
    Map<String, Object> props = new HashMap<>();
    props.put("name", map.getName());
    props.put("tilesets", new ArrayList<>(map.getTilesets()));
    for (Map.Entry<String, ICustomProperty> entry : map.getProperties().entrySet()) {
      props.put("prop:" + entry.getKey(), new CustomProperty(entry.getValue()));
    }
    return props;
  }

  private static List<LayerNode> snapshotLayerStructure(ILayerList layers) {
    return layers.getRenderLayers().stream().map(layer -> new LayerNode(
      layer,
      layer instanceof IGroupLayer group ? snapshotLayerStructure(group) : List.of())).toList();
  }

  private List<LayerPropertiesNode> snapshotLayerTreeProperties(ILayerList layers) {
    return layers.getRenderLayers().stream().map(layer -> new LayerPropertiesNode(
      layer,
      snapshotLayerProperties(layer),
      layer instanceof IGroupLayer group ? snapshotLayerTreeProperties(group) : List.of())).toList();
  }

  private static void restoreLayerStructure(IMap map, List<LayerNode> layers) {
    restoreLayerStructure((ILayerList) map, layers);
    Editor.instance().getMapComponent().synchronizeEnvironmentEntities(map);
  }

  private static void restoreLayerStructure(ILayerList parent, List<LayerNode> layers) {
    for (ILayer layer : new ArrayList<>(parent.getRenderLayers())) {
      parent.removeLayer(layer);
    }
    for (LayerNode node : layers) {
      parent.addLayer(node.layer());
      if (node.layer() instanceof IGroupLayer group) {
        restoreLayerStructure(group, node.children());
      }
    }
  }

  private record LayerNode(ILayer layer, List<LayerNode> children) {
  }

  private static void restoreLayerProperties(List<LayerPropertiesNode> layers) {
    for (LayerPropertiesNode node : layers) {
      restoreLayerProperties(node.layer(), node.properties());
      restoreLayerProperties(node.children());
    }
  }

  private record LayerPropertiesNode(
      ILayer layer, Map<String, Object> properties, List<LayerPropertiesNode> children) {
  }

  private static final class ImageSnapshot {
    private final MapImage image;
    private final Map<String, String> properties;

    private ImageSnapshot(MapImage image) {
      this.image = new MapImage(image);
      this.properties = image.getProperties().entrySet().stream().collect(
        java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getAsString()));
    }

    private MapImage copy() {
      return new MapImage(this.image);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof ImageSnapshot other)) {
        return false;
      }
      return Objects.equals(this.image.getSource(), other.image.getSource())
        && Objects.equals(this.image.getAbsoluteSourcePath(), other.image.getAbsoluteSourcePath())
        && Objects.equals(this.image.getTransparentColor(), other.image.getTransparentColor())
        && this.image.getWidth() == other.image.getWidth()
        && this.image.getHeight() == other.image.getHeight()
        && Objects.equals(this.properties, other.properties);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
        this.image.getSource(), this.image.getAbsoluteSourcePath(), this.image.getTransparentColor(),
        this.image.getWidth(), this.image.getHeight(), this.properties);
    }
  }

  private void restoreMapProperties(IMap map, Map<String, Object> props) {
    if (!Editor.instance().getMapComponent().renameMap(map, (String) props.get("name"))) {
      throw new IllegalStateException("Cannot restore map name because it is no longer unique.");
    }
    if (props.get("tilesets") instanceof List<?> tilesets) {
      map.getTilesets().clear();
      for (Object tileset : tilesets) {
        if (tileset instanceof de.gurkenlabs.litiengine.environment.tilemap.ITileset value) {
          map.getTilesets().add(value);
        }
      }
      UI.mapTilesetsChanged(map);
    }
    map.getProperties().keySet().removeIf(k -> !k.equals("name"));
    for (Map.Entry<String, Object> entry : props.entrySet()) {
      if (entry.getKey().startsWith("prop:")) {
        map.setValue(entry.getKey().substring(5), new CustomProperty((ICustomProperty) entry.getValue()));
      }
    }
    if (Game.world().environment() != null && Game.world().environment().getMap() == map) {
      if (Game.world().environment().getAmbientLight() != null) {
        Game.world().environment().getAmbientLight().setColor(map.getColorValue(MapProperty.AMBIENTCOLOR, AmbientLight.DEFAULT_COLOR));
      }
      if (Game.world().environment().getStaticShadowLayer() != null) {
        Game.world().environment().getStaticShadowLayer().setColor(map.getColorValue(MapProperty.SHADOWCOLOR, StaticShadow.DEFAULT_COLOR));
      }
    }
  }

  public void layerChanging(ILayer layer) {
    if (executing || layer == null) {
      return;
    }
    this.changingLayerSnapshot = snapshotLayerProperties(layer);
    this.changingLayer = layer;
  }

  public void layerChanged(ILayer layer) {
    if (executing || layer == null || this.changingLayer != layer) {
      return;
    }

    Map<String, Object> newProps = snapshotLayerProperties(layer);
    if (newProps.equals(this.changingLayerSnapshot)) {
      this.changingLayer = null;
      this.changingLayerSnapshot = null;
      return;
    }
    this.ensureStackSize();
    this.currentIndex++;
    this.clearRedoSteps();
    this.undoStack[this.currentIndex] =
      new UndoState(layer, this.changingLayerSnapshot, newProps, OperationType.LAYER_CHANGE, this.operation);
    this.changingLayer = null;
    this.changingLayerSnapshot = null;
    fireUndoStackChangedEvent(this);
  }

  public void mapChanging(IMap map) {
    if (executing || map == null) {
      return;
    }
    this.changingMapSnapshot = snapshotMapProperties(map);
    this.changingMap = map;
  }

  public void mapChanged(IMap map) {
    if (executing || map == null || this.changingMap != map) {
      return;
    }

    Map<String, Object> newProps = snapshotMapProperties(map);
    if (newProps.equals(this.changingMapSnapshot)) {
      this.changingMap = null;
      this.changingMapSnapshot = null;
      return;
    }
    this.ensureStackSize();
    this.currentIndex++;
    this.clearRedoSteps();
    this.undoStack[this.currentIndex] =
      new UndoState(map, this.changingMapSnapshot, newProps, OperationType.MAP_CHANGE, this.operation);
    this.changingMap = null;
    this.changingMapSnapshot = null;
    fireUndoStackChangedEvent(this);
  }

  public void layersChanging(IMap map) {
    if (executing || map == null) {
      return;
    }
    this.changingLayersSnapshot = snapshotLayerTreeProperties(map);
    this.changingLayersMap = map;
  }

  public void layersChanged(IMap map) {
    if (executing || map == null || this.changingLayersMap != map) {
      return;
    }
    List<LayerPropertiesNode> newProperties = snapshotLayerTreeProperties(map);
    if (sameLayerProperties(this.changingLayersSnapshot, newProperties)) {
      this.changingLayersMap = null;
      this.changingLayersSnapshot = null;
      return;
    }
    this.ensureStackSize();
    this.currentIndex++;
    this.clearRedoSteps();
    this.undoStack[this.currentIndex] =
      new UndoState(map, this.changingLayersSnapshot, newProperties, this.operation, true);
    this.changingLayersMap = null;
    this.changingLayersSnapshot = null;
    fireUndoStackChangedEvent(this);
  }

  private static boolean sameLayerProperties(
      List<LayerPropertiesNode> first, List<LayerPropertiesNode> second) {
    Map<ILayer, Map<String, Object>> firstProperties = new IdentityHashMap<>();
    Map<ILayer, Map<String, Object>> secondProperties = new IdentityHashMap<>();
    collectLayerProperties(first, firstProperties);
    collectLayerProperties(second, secondProperties);
    return firstProperties.equals(secondProperties);
  }

  private static void collectLayerProperties(
      List<LayerPropertiesNode> nodes, Map<ILayer, Map<String, Object>> properties) {
    for (LayerPropertiesNode node : nodes) {
      properties.put(node.layer(), node.properties());
      collectLayerProperties(node.children(), properties);
    }
  }

  public void layerStructureChanging(IMap map) {
    if (executing || map == null) {
      return;
    }
    this.changingLayerStructureSnapshot = snapshotLayerStructure(map);
    this.changingLayerStructureMap = map;
  }

  public void layerStructureChanged(IMap map) {
    if (executing || map == null || this.changingLayerStructureMap != map) {
      return;
    }

    List<LayerNode> newStructure = snapshotLayerStructure(map);
    if (newStructure.equals(this.changingLayerStructureSnapshot)) {
      this.changingLayerStructureMap = null;
      this.changingLayerStructureSnapshot = null;
      return;
    }

    this.ensureStackSize();
    this.currentIndex++;
    this.clearRedoSteps();

    this.undoStack[this.currentIndex] =
      new UndoState(map, this.changingLayerStructureSnapshot, newStructure, this.operation);
    this.changingLayerStructureMap = null;
    this.changingLayerStructureSnapshot = null;
    fireUndoStackChangedEvent(this);
  }

  /**
   * Registers a consumer to be called whenever the undo stack changes.
   *
   * @param cons The consumer to be called on undo stack change.
   */
  public static void onUndoStackChanged(Consumer<UndoManager> cons) {
    undoStackChangedConsumers.add(cons);
  }

  /**
   * Unregisters a previously registered undo stack change consumer.
   *
   * @param cons The consumer to unregister.
   */
  public static void removeUndoStackChanged(Consumer<UndoManager> cons) {
    undoStackChangedConsumers.remove(cons);
  }

  /**
   * Registers a consumer to be called whenever a map object is added.
   *
   * @param cons The consumer to be called on map object addition.
   */
  public static void onMapObjectAdded(Consumer<UndoManager> cons) {
    mapObjectAdded.add(cons);
  }

  /**
   * Registers a consumer to be called whenever a map object is removed.
   *
   * @param cons The consumer to be called on map object removal.
   */
  public static void onMapObjectRemoved(Consumer<UndoManager> cons) {
    mapObjectRemoved.add(cons);
  }

  /**
   * Checks if the specified map has unsaved changes.
   *
   * @param map The map to check for unsaved changes.
   * @return True if the map has unsaved changes, false otherwise.
   */
  public static boolean hasChanges(IMap map) {
    if (map == null) {
      return false;
    }

    if (instance.containsKey(map)) {
      UndoManager manager = instance.get(map);
      return manager.untrackedChanges || manager.currentIndex != manager.savedIndex;
    }

    return false;
  }

  /**
   * Marks the specified map as saved and refreshes the map controller UI.
   *
   * @param map The map to be marked as saved.
   */
  public static void save(IMap map) {
    if (instance.containsKey(map)) {
      UndoManager manager = instance.get(map);
      manager.savedIndex = manager.currentIndex;
      manager.untrackedChanges = false;
      if (UI.getMapController() != null) {
        UI.getMapController().refresh();
      }
    }
  }

  private static void fireUndoStackChangedEvent(UndoManager undoManager) {
    undoManager.revision++;
    if (undoManager.eventAggregationDepth > 0) {
      undoManager.undoStackChangedPending = true;
      return;
    }

    fireUndoManagerEvent(undoStackChangedConsumers, undoManager);
  }

  private void beginEventAggregation() {
    this.eventAggregationDepth++;
  }

  private void endEventAggregation() {
    if (this.eventAggregationDepth == 0 || --this.eventAggregationDepth > 0 || !this.undoStackChangedPending) {
      return;
    }

    this.undoStackChangedPending = false;
    fireUndoManagerEvent(undoStackChangedConsumers, this);
  }

  private static void fireUndoManagerEvent(
    List<Consumer<UndoManager>> consumers, UndoManager undoManager) {
    for (Consumer<UndoManager> cons : consumers) {
      cons.accept(undoManager);
    }
  }

  private static void restoreState(IMapObject target, IMapObject restore) {
    IMapObjectLayer targetLayer = target.getLayer();
    IMapObjectLayer restoreLayer = restore.getLayer();
    if (targetLayer != restoreLayer) {
      if (targetLayer != null) {
        targetLayer.removeMapObject(target);
      }
      if (restoreLayer != null) {
        restoreLayer.addMapObject(target);
      }
    }

    target.setId(restore.getId());
    target.setName(restore.getName());
    target.setType(restore.getType());
    target.setX(restore.getX());
    target.setY(restore.getY());
    target.setWidth(restore.getWidth());
    target.setHeight(restore.getHeight());
    target.getProperties().clear();
    for (Map.Entry<String, ICustomProperty> prop : restore.getProperties().entrySet()) {
      target.setValue(prop.getKey(), prop.getValue());
    }
  }

  /**
   * Refreshes the affected targets by reloading them from the map and updating the UI components.
   *
   * @param affectedTargets The list of map objects that were affected by the undo or redo operation.
   */
  private static void refreshAffectedTargets(List<IMapObject> affectedTargets) {
    for (IMapObject target : affectedTargets) {
      Game.world().environment().reloadFromMap(target.getId());

      if (Editor.instance().getMapComponent().getFocusedMapObject() != null
        && Editor.instance().getMapComponent().getFocusedMapObject().getId() == target.getId()) {
        Editor.instance().getMapComponent().refreshInspector();
        UI.getEntityController().select(target);
      }
    }
  }

  private static void refreshLayerAndMapViews(ILayer affectedLayer, boolean affectedMap) {
    if (UI.getLayerController() instanceof de.gurkenlabs.utiliti.view.components.SceneGraph sg) {
      sg.refresh();
    }
    if (affectedLayer != null) {
      UI.showLayerProperties(affectedLayer);
    } else if (affectedMap) {
      UI.showMapProperties();
    }
  }

  /**
   * Ensures that the undo stack does not exceed the maximum stack size. If the stack is full, it shifts all elements to the left by one index,
   * effectively removing the oldest undo state to make room for a new one.
   */
  private void ensureStackSize() {
    // move undo states by one index
    while (this.currentIndex >= MAX_STACK_SIZE - 1) {
      System.arraycopy(this.undoStack, 1, this.undoStack, 0, MAX_STACK_SIZE - 1);
      this.undoStack[MAX_STACK_SIZE - 1] = null;
      this.currentIndex--;
      if (this.savedIndex > 0) {
        this.savedIndex--;
      } else {
        this.savedIndex = Integer.MIN_VALUE;
      }
    }
  }

  /**
   * Clears all redo steps from the undo stack. This method is called whenever a new UndoState is added while in the middle of the current stack,
   * ensuring that all future redo steps are removed because the new state will now be the last element.
   */
  private void clearRedoSteps() {
    if (this.savedIndex >= this.currentIndex) {
      this.savedIndex = Integer.MIN_VALUE;
    }
    for (int index = this.currentIndex + 1; index < MAX_STACK_SIZE && this.undoStack[index] != null; index++) {
      this.undoStack[index] = null;
    }
  }

  /**
   * Gets the name of the map associated with this UndoManager.
   *
   * @return The name of the map.
   */
  public String getMapName() {
    return this.map.getName();
  }

  /**
   * Represents the type of operation that can be performed in the undo manager.
   */
  private enum OperationType {
    CHANGE,
    MOVE,
    RESIZE,
    ADD,
    DELETE,
    LAYER_CHANGE,
    LAYER_TREE_CHANGE,
    MAP_CHANGE,
    LAYER_STRUCTURE_CHANGE,
    RESOURCE_CHANGE
  }

  /**
   * Represents the state of an undoable operation.
   */
  public static class UndoState {
    private final IMapObject target;
    private final IMapObject oldMapObject;
    private final IMapObject newMapObject;
    private final IMapObjectLayer layer;
    private final OperationType operationType;
    private final int operation;
    private final ILayer targetLayer;
    private final IMap targetMap;
    private final Map<String, Object> oldLayerProperties;
    private final Map<String, Object> newLayerProperties;
    private final Map<String, Object> oldMapProperties;
    private final Map<String, Object> newMapProperties;
    private final IMap targetLayerStructureMap;
    private final List<LayerNode> oldLayerStructure;
    private final List<LayerNode> newLayerStructure;
    private final List<LayerPropertiesNode> oldLayerTreeProperties;
    private final List<LayerPropertiesNode> newLayerTreeProperties;
    private final Runnable undoResourceAction;
    private final Runnable redoResourceAction;

    /**
     * Constructs an UndoState with the specified parameters.
     *
     * @param target        The target map object associated with this undo state.
     * @param operationType The type of operation (ADD, CHANGE, DELETE).
     * @param operation     The operation identifier.
     */
    public UndoState(IMapObject target, OperationType operationType, int operation) {
      this.operation = operation;
      this.target = target;
      this.layer = Game.world().environment().getMap().getMapObjectLayer(target);
      this.oldMapObject = null;
      this.newMapObject = null;
      this.operationType = operationType;
      this.targetLayer = null;
      this.targetMap = null;
      this.oldLayerProperties = null;
      this.newLayerProperties = null;
      this.oldMapProperties = null;
      this.newMapProperties = null;
      this.targetLayerStructureMap = null;
      this.oldLayerStructure = null;
      this.newLayerStructure = null;
      this.oldLayerTreeProperties = null;
      this.newLayerTreeProperties = null;
      this.undoResourceAction = null;
      this.redoResourceAction = null;
    }

    public UndoState(
      IMapObject target,
      IMapObject oldMapObject,
      IMapObject newMapObject,
      OperationType operationType,
      int operation) {
      this.operation = operation;
      this.target = target;
      this.oldMapObject = operationType != OperationType.ADD ? oldMapObject : null;
      this.newMapObject = operationType != OperationType.DELETE ? newMapObject : null;
      this.operationType = operationType;
      this.layer = Game.world().environment().getMap().getMapObjectLayer(target);
      this.targetLayer = null;
      this.targetMap = null;
      this.oldLayerProperties = null;
      this.newLayerProperties = null;
      this.oldMapProperties = null;
      this.newMapProperties = null;
      this.targetLayerStructureMap = null;
      this.oldLayerStructure = null;
      this.newLayerStructure = null;
      this.oldLayerTreeProperties = null;
      this.newLayerTreeProperties = null;
      this.undoResourceAction = null;
      this.redoResourceAction = null;
    }

    public UndoState(
      ILayer targetLayer,
      Map<String, Object> oldProps,
      Map<String, Object> newProps,
      OperationType operationType,
      int operation) {
      this.operation = operation;
      this.target = null;
      this.oldMapObject = null;
      this.newMapObject = null;
      this.layer = null;
      this.operationType = operationType;
      this.targetLayer = targetLayer;
      this.targetMap = null;
      this.oldLayerProperties = oldProps;
      this.newLayerProperties = newProps;
      this.oldMapProperties = null;
      this.newMapProperties = null;
      this.targetLayerStructureMap = null;
      this.oldLayerStructure = null;
      this.newLayerStructure = null;
      this.oldLayerTreeProperties = null;
      this.newLayerTreeProperties = null;
      this.undoResourceAction = null;
      this.redoResourceAction = null;
    }

    public UndoState(
      IMap targetMap,
      Map<String, Object> oldProps,
      Map<String, Object> newProps,
      OperationType operationType,
      int operation) {
      this.operation = operation;
      this.target = null;
      this.oldMapObject = null;
      this.newMapObject = null;
      this.layer = null;
      this.operationType = operationType;
      this.targetLayer = null;
      this.targetMap = targetMap;
      this.oldLayerProperties = null;
      this.newLayerProperties = null;
      this.oldMapProperties = oldProps;
      this.newMapProperties = newProps;
      this.targetLayerStructureMap = null;
      this.oldLayerStructure = null;
      this.newLayerStructure = null;
      this.oldLayerTreeProperties = null;
      this.newLayerTreeProperties = null;
      this.undoResourceAction = null;
      this.redoResourceAction = null;
    }

    public UndoState(
      IMap targetMap,
      List<LayerNode> oldLayerStructure,
      List<LayerNode> newLayerStructure,
      int operation) {
      this.operation = operation;
      this.target = null;
      this.oldMapObject = null;
      this.newMapObject = null;
      this.layer = null;
      this.operationType = OperationType.LAYER_STRUCTURE_CHANGE;
      this.targetLayer = null;
      this.targetMap = null;
      this.oldLayerProperties = null;
      this.newLayerProperties = null;
      this.oldMapProperties = null;
      this.newMapProperties = null;
      this.targetLayerStructureMap = targetMap;
      this.oldLayerStructure = oldLayerStructure;
      this.newLayerStructure = newLayerStructure;
      this.oldLayerTreeProperties = null;
      this.newLayerTreeProperties = null;
      this.undoResourceAction = null;
      this.redoResourceAction = null;
    }

    private UndoState(
      IMap targetMap,
      List<LayerPropertiesNode> oldProperties,
      List<LayerPropertiesNode> newProperties,
      int operation,
      boolean layerProperties) {
      this.operation = operation;
      this.target = null;
      this.oldMapObject = null;
      this.newMapObject = null;
      this.layer = null;
      this.operationType = OperationType.LAYER_TREE_CHANGE;
      this.targetLayer = null;
      this.targetMap = targetMap;
      this.oldLayerProperties = null;
      this.newLayerProperties = null;
      this.oldMapProperties = null;
      this.newMapProperties = null;
      this.targetLayerStructureMap = null;
      this.oldLayerStructure = null;
      this.newLayerStructure = null;
      this.oldLayerTreeProperties = oldProperties;
      this.newLayerTreeProperties = newProperties;
      this.undoResourceAction = null;
      this.redoResourceAction = null;
    }

    public UndoState(Runnable undoResourceAction, Runnable redoResourceAction, int operation) {
      this.operation = operation;
      this.target = null;
      this.oldMapObject = null;
      this.newMapObject = null;
      this.layer = null;
      this.operationType = OperationType.RESOURCE_CHANGE;
      this.targetLayer = null;
      this.targetMap = null;
      this.oldLayerProperties = null;
      this.newLayerProperties = null;
      this.oldMapProperties = null;
      this.newMapProperties = null;
      this.targetLayerStructureMap = null;
      this.oldLayerStructure = null;
      this.newLayerStructure = null;
      this.oldLayerTreeProperties = null;
      this.newLayerTreeProperties = null;
      this.undoResourceAction = undoResourceAction;
      this.redoResourceAction = redoResourceAction;
    }

    /**
     * Returns a string representation of the UndoState.
     *
     * @return A string in the format "name(id) operationType".
     */
    @Override
    public String toString() {
      return this.description();
    }

    private String description() {
      if (target != null) {
        String name = target.getName();
        String object = name != null && !name.isBlank()
          ? name
          : Resources.strings().get(
              "history_objectFallback",
              target.getType() != null && !target.getType().isBlank()
                  ? target.getType()
                  : Resources.strings().get("history_object"),
              Integer.toString(target.getId()));
        return switch (this.operationType) {
          case ADD -> Resources.strings().get("history_addObject", object);
          case DELETE -> Resources.strings().get("history_deleteObject", object);
          case MOVE -> Resources.strings().get("history_moveObject", object);
          case RESIZE -> Resources.strings().get("history_resizeObject", object);
          default -> Resources.strings().get("history_changeObject", object);
        };
      }
      if (targetLayer != null) {
        return Resources.strings().get("history_changeLayer", targetLayer.getName());
      }
      if (targetMap != null) {
        if (this.operationType == OperationType.LAYER_TREE_CHANGE) {
          return Resources.strings().get("history_changeLayer", targetMap.getName());
        }
        return Resources.strings().get("history_changeMap", targetMap.getName());
      }
      if (targetLayerStructureMap != null) {
        return Resources.strings().get("history_changeLayerOrder", targetLayerStructureMap.getName());
      }
      return Resources.strings().get("history_editResource");
    }

    public int getOperation() {
      return this.operation;
    }

    public ILayer getTargetLayer() {
      return this.targetLayer;
    }

    public IMap getTargetMap() {
      return this.targetMap;
    }
  }
}
