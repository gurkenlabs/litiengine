package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.utiliti.view.components.UI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
  private final String mapName;
  private int operation = 0;
  private boolean saved = true;
  private boolean executing;
  private ILayer changingLayer;
  private Map<String, Object> changingLayerSnapshot;
  private IMap changingMap;
  private Map<String, Object> changingMapSnapshot;

  private static final HashMap<String, UndoManager> instance;
  private static final List<Consumer<UndoManager>> undoStackChangedConsumers;
  private static final List<Consumer<UndoManager>> mapObjectAdded;
  private static final List<Consumer<UndoManager>> mapObjectRemoved;

  private UndoManager(String mapName) {
    this.changing = new CopyOnWriteArrayList<>();
    this.undoStack = new UndoState[MAX_STACK_SIZE];
    this.mapName = mapName;
  }

  static {
    instance = new HashMap<>();
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
    if (instance.containsKey(Game.world().environment().getMap().getName())) {
      return instance.get(Game.world().environment().getMap().getName());
    }

    final String mapName = Game.world().environment().getMap().getName();
    UndoManager newUndoManager = new UndoManager(mapName);
    instance.put(mapName, newUndoManager);
    return newUndoManager;
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
    this.operation = nextOperation;
  }

  /**
   * Ends the current operation by incrementing the next operation identifier and resetting the current operation.
   */
  public void endOperation() {
    ++nextOperation;
    this.operation = 0;
  }

  /**
   * Undoes the last operation. If there are no operations to undo or if an operation is currently being executed, this method returns immediately.
   */
  public void undo() {
    if (this.executing || this.currentIndex < 0) {
      return;
    }

    final int currentOperation = this.undoStack[this.currentIndex].getOperation();

    int stepsUndone = 0;
    this.executing = true;
    try {
      List<IMapObject> affectedTargets = new ArrayList<>();
      boolean affectedLayer = false;
      boolean affectedMap = false;
      do {
        stepsUndone++;
        final UndoState state = this.undoStack[this.currentIndex];
        if (state.target != null && affectedTargets.stream().noneMatch(m -> m.getId() == state.target.getId())) {
          affectedTargets.add(state.target);
        }
        if (state.targetLayer != null) {
          affectedLayer = true;
        }
        if (state.targetMap != null) {
          affectedMap = true;
        }

        switch (state.operationType) {
          case ADD -> Editor.instance().getMapComponent().delete(state.target);
          case CHANGE -> restoreState(state.target, Objects.requireNonNull(state.oldMapObject));
          case DELETE -> Editor.instance().getMapComponent().add(state.target, state.layer);
          case LAYER_CHANGE -> restoreLayerProperties(state.targetLayer, state.oldLayerProperties);
          case MAP_CHANGE -> restoreMapProperties(state.targetMap, state.oldMapProperties);
        }

        this.currentIndex--;
      } while (currentOperation != 0
        && this.currentIndex >= 0
        && this.undoStack[this.currentIndex].getOperation() == currentOperation);

      log.log(Level.FINE, "{0} steps undone.", stepsUndone);
      refreshAffectedTargets(affectedTargets);
      if (affectedLayer || affectedMap) {
        refreshLayerAndMapViews();
      }
      fireUndoStackChangedEvent(this);
    } finally {
      this.executing = false;
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

    int stepsRedone = 0;
    this.executing = true;
    try {
      List<IMapObject> affectedTargets = new ArrayList<>();
      boolean affectedLayer = false;
      boolean affectedMap = false;
      do {
        ++stepsRedone;
        ++this.currentIndex;

        final UndoState state = this.undoStack[this.currentIndex];
        if (state.target != null && affectedTargets.stream().noneMatch(m -> m.getId() == state.target.getId())) {
          affectedTargets.add(state.target);
        }
        if (state.targetLayer != null) {
          affectedLayer = true;
        }
        if (state.targetMap != null) {
          affectedMap = true;
        }

        switch (state.operationType) {
          case ADD -> Editor.instance().getMapComponent().add(state.target, state.layer);
          case CHANGE -> restoreState(state.target, Objects.requireNonNull(state.newMapObject));
          case DELETE -> Editor.instance().getMapComponent().delete(state.target);
          case LAYER_CHANGE -> restoreLayerProperties(state.targetLayer, state.newLayerProperties);
          case MAP_CHANGE -> restoreMapProperties(state.targetMap, state.newMapProperties);
        }
      } while (currentOperation != 0
        && this.currentIndex < MAX_STACK_SIZE
        && this.undoStack[this.currentIndex + 1] != null
        && this.undoStack[this.currentIndex + 1].getOperation() == currentOperation);

      log.log(Level.FINE, "{0} steps redone.", stepsRedone);

      refreshAffectedTargets(affectedTargets);
      if (affectedLayer || affectedMap) {
        refreshLayerAndMapViews();
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

    this.mapObjectChanged(mapObject, mapObject.getId());
  }

  /**
   * Updates the state of a map object after it has been changed.
   *
   * @param mapObject     The map object that has been changed.
   * @param previousMapId The previous ID of the map object before the change.
   */
  public void mapObjectChanged(IMapObject mapObject, int previousMapId) {
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
        OperationType.CHANGE,
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
    for (Map.Entry<String, ICustomProperty> entry : layer.getProperties().entrySet()) {
      props.put("prop:" + entry.getKey(), entry.getValue().getAsString());
    }
    return props;
  }

  private void restoreLayerProperties(ILayer layer, Map<String, Object> props) {
    layer.setName((String) props.get("name"));
    layer.setOpacity((float) props.get("opacity"));
    layer.setVisible((boolean) props.get("visible"));
    layer.setTintColor((java.awt.Color) props.get("tintColor"));
    layer.setRenderType((de.gurkenlabs.litiengine.graphics.RenderType) props.get("renderType"));
    if (layer instanceof IMapObjectLayer mol && props.containsKey("color")) {
      mol.setColor((java.awt.Color) props.get("color"));
    }
    layer.getProperties().keySet().removeIf(k -> true);
    for (Map.Entry<String, Object> entry : props.entrySet()) {
      if (entry.getKey().startsWith("prop:")) {
        layer.setValue(entry.getKey().substring(5), (String) entry.getValue());
      }
    }
  }

  private Map<String, Object> snapshotMapProperties(IMap map) {
    Map<String, Object> props = new HashMap<>();
    props.put("name", map.getName());
    for (Map.Entry<String, ICustomProperty> entry : map.getProperties().entrySet()) {
      props.put("prop:" + entry.getKey(), entry.getValue().getAsString());
    }
    return props;
  }

  private void restoreMapProperties(IMap map, Map<String, Object> props) {
    map.setName((String) props.get("name"));
    map.getProperties().keySet().removeIf(k -> !k.equals("name"));
    for (Map.Entry<String, Object> entry : props.entrySet()) {
      if (entry.getKey().startsWith("prop:")) {
        map.setValue(entry.getKey().substring(5), (String) entry.getValue());
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

    this.ensureStackSize();
    this.currentIndex++;
    this.clearRedoSteps();

    Map<String, Object> newProps = snapshotLayerProperties(layer);
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

    this.ensureStackSize();
    this.currentIndex++;
    this.clearRedoSteps();

    Map<String, Object> newProps = snapshotMapProperties(map);
    this.undoStack[this.currentIndex] =
      new UndoState(map, this.changingMapSnapshot, newProps, OperationType.MAP_CHANGE, this.operation);
    this.changingMap = null;
    this.changingMapSnapshot = null;
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
    if (instance.containsKey(map.getName())) {
      return !instance.get(map.getName()).saved;
    }

    return false;
  }

  /**
   * Marks the specified map as saved and refreshes the map controller UI.
   *
   * @param map The map to be marked as saved.
   */
  public static void save(IMap map) {
    if (instance.containsKey(map.getName())) {
      instance.get(map.getName()).saved = true;
      UI.getMapController().refresh();
    }
  }

  private static void fireUndoStackChangedEvent(UndoManager undoManager) {
    undoManager.saved = false;
    fireUndoManagerEvent(undoStackChangedConsumers, undoManager);
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
        UI.getInspector().bind(target);
        UI.getEntityController().select(target);
      }
    }
  }

  private static void refreshLayerAndMapViews() {
    if (UI.getLayerController() instanceof de.gurkenlabs.utiliti.view.components.SceneGraph sg) {
      sg.refresh();
    }
    UI.showMapProperties();
  }

  /**
   * Ensures that the undo stack does not exceed the maximum stack size. If the stack is full, it shifts all elements to the left by one index,
   * effectively removing the oldest undo state to make room for a new one.
   */
  private void ensureStackSize() {
    // move undo states by one index
    if (this.currentIndex == MAX_STACK_SIZE - 1) {
      System.arraycopy(this.undoStack, 1, this.undoStack, 0, MAX_STACK_SIZE - 1);
      this.undoStack[MAX_STACK_SIZE - 1] = null;
      this.currentIndex--;
    }
  }

  /**
   * Clears all redo steps from the undo stack. This method is called whenever a new UndoState is added while in the middle of the current stack,
   * ensuring that all future redo steps are removed because the new state will now be the last element.
   */
  private void clearRedoSteps() {
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
    return mapName;
  }

  /**
   * Represents the type of operation that can be performed in the undo manager.
   */
  private enum OperationType {
    CHANGE,
    ADD,
    DELETE,
    LAYER_CHANGE,
    MAP_CHANGE
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
    }

    /**
     * Returns a string representation of the UndoState.
     *
     * @return A string in the format "name(id) operationType".
     */
    @Override
    public String toString() {
      if (target != null) {
        return target.getName() + "(" + target.getId() + ") " + this.operationType.toString();
      }
      if (targetLayer != null) {
        return "Layer:" + targetLayer.getName() + " " + this.operationType.toString();
      }
      if (targetMap != null) {
        return "Map:" + targetMap.getName() + " " + this.operationType.toString();
      }
      return this.operationType.toString();
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
