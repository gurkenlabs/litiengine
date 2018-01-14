package de.gurkenlabs.utiliti;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Property;

public class UndoManager {
  private static final int MAX_STACK_SIZE = 10;
  private UndoState[] undoStack;
  private List<IMapObject> changing;
  private int currentIndex = -1;
  private final String mapName;

  private static HashMap<String, UndoManager> instance;
  private static List<Consumer<UndoManager>> undoStackChangedConsumers;

  private UndoManager(String mapName) {
    this.changing = new CopyOnWriteArrayList<>();
    this.undoStack = new UndoState[MAX_STACK_SIZE];
    this.mapName = mapName;
  }

  static {
    instance = new HashMap<>();
    undoStackChangedConsumers = new CopyOnWriteArrayList<>();
  }

  public static UndoManager instance() {
    if (instance.containsKey(Game.getEnvironment().getMap().getName())) {
      return instance.get(Game.getEnvironment().getMap().getName());
    }

    UndoManager newUndoManager = new UndoManager(Game.getEnvironment().getMap().getName());
    instance.put(Game.getEnvironment().getMap().getName(), newUndoManager);
    return newUndoManager;
  }

  public void undo() {
    if (this.currentIndex < 0) {
      return;
    }

    UndoState state = this.undoStack[this.currentIndex];
    switch (state.operationType) {
    case ADD:
      EditorScreen.instance().getMapComponent().delete(state.target);
      break;
    case CHANGE:
      restoreState(state.target, state.oldMapObject);
      break;
    case DELETE:
      EditorScreen.instance().getMapComponent().add(state.target, state.layer);
      break;
    }

    this.currentIndex--;

    fireUndoStackChangedEvent(this);
  }

  public void redo() {
    if (this.undoStack[this.currentIndex + 1] == null) {
      return;
    }

    if (this.currentIndex >= this.undoStack.length - 1) {
      this.currentIndex = this.undoStack.length - 1;
      return;
    }

    this.currentIndex++;
    UndoState state = this.undoStack[this.currentIndex];

    switch (state.operationType) {
    case ADD:
      EditorScreen.instance().getMapComponent().add(state.target, state.layer);
      break;
    case CHANGE:
      restoreState(state.target, state.newMapObject);
      break;
    case DELETE:
      EditorScreen.instance().getMapComponent().delete(state.target);
      break;
    }

    fireUndoStackChangedEvent(this);
  }

  public boolean canUndo() {
    return this.currentIndex >= 0;
  }

  public boolean canRedo() {
    return this.currentIndex < MAX_STACK_SIZE - 1 && this.undoStack[this.currentIndex + 1] != null;
  }

  public UndoState[] getUndoStack() {
    return this.undoStack;
  }

  public void mapObjectChanging(IMapObject mapObject) {
    if (mapObject == null) {
      return;
    }

    if (this.changing.contains(mapObject)) {
      // the old state is already tracked, while multiple changes are carried out, we don't want to track the steps in between
      return;
    }

    this.changing.add(clone(mapObject));
  }

  public void mapObjectChanged(IMapObject mapObject) {
    if (mapObject == null) {
      return;
    }

    Optional<IMapObject> trackedMapObject = this.changing.stream().filter(x -> x.getId() == mapObject.getId()).findFirst();
    if (!trackedMapObject.isPresent()) {
      // didn't track the changing event and therefore cannot provide an undo
      return;
    }

    this.ensureStackSize();
    this.currentIndex++;

    this.undoStack[this.currentIndex] = new UndoState(mapObject, this.changing.remove(this.changing.indexOf(trackedMapObject.get())), clone(mapObject), OperationType.CHANGE);
    fireUndoStackChangedEvent(this);
  }

  public void mapObjectDeleting(IMapObject mapObject) {
    if (mapObject == null) {
      return;
    }

    this.ensureStackSize();
    this.currentIndex++;

    this.undoStack[this.currentIndex] = new UndoState(mapObject, OperationType.DELETE);
    fireUndoStackChangedEvent(this);
  }

  public void mapObjectAdded(IMapObject mapObject) {
    if (mapObject == null) {
      return;
    }

    this.ensureStackSize();
    this.currentIndex++;
    this.undoStack[this.currentIndex] = new UndoState(mapObject, OperationType.ADD);
    fireUndoStackChangedEvent(this);
  }

  public static void onUndoStackChanged(Consumer<UndoManager> cons) {
    undoStackChangedConsumers.add(cons);
  }

  private static final void fireUndoStackChangedEvent(UndoManager undoManager) {
    for (Consumer<UndoManager> cons : undoStackChangedConsumers) {
      cons.accept(undoManager);
    }
  }

  private static void restoreState(IMapObject target, IMapObject restore) {
    target.setId(restore.getId());
    target.setName(restore.getName());
    target.setType(restore.getType());
    target.setX(restore.getX());
    target.setY(restore.getY());
    target.setWidth(restore.getWidth());
    target.setHeight(restore.getHeight());
    target.getAllCustomProperties().clear();
    for (Property prop : restore.getAllCustomProperties()) {
      target.setCustomProperty(prop.getName(), prop.getValue());
    }

    Game.getEnvironment().reloadFromMap(target.getId());
    if (MapObjectType.get(target.getType()) == MapObjectType.LIGHTSOURCE) {
      Game.getEnvironment().getAmbientLight().createImage();
    }

    EditorScreen.instance().getMapComponent().setFocus(target);
  }

  private static IMapObject clone(IMapObject mapObject) {
    MapObject clonedObject = new MapObject();
    clonedObject.setId(mapObject.getId());
    clonedObject.setName(mapObject.getName() != null ? mapObject.getName() : "");
    clonedObject.setType(mapObject.getType() != null ? mapObject.getType() : "");

    clonedObject.setX(mapObject.getX());
    clonedObject.setY(mapObject.getY());
    clonedObject.setWidth(mapObject.getWidth());
    clonedObject.setHeight(mapObject.getHeight());
    clonedObject.setCustomProperties(mapObject.getAllCustomProperties().stream().collect(Collectors.toList()));

    return clonedObject;
  }

  private void ensureStackSize() {
    // move undo states by one index
    if (this.currentIndex == MAX_STACK_SIZE - 1) {
      for (int i = 0; i < MAX_STACK_SIZE; i++) {
        if (i == MAX_STACK_SIZE - 1) {
          this.undoStack[i] = null;
          break;
        }

        this.undoStack[i] = this.undoStack[i + 1];
      }
      this.currentIndex--;
    }
  }

  public String getMapName() {
    return mapName;
  }

  private enum OperationType {
    CHANGE,
    ADD,
    DELETE
  }

  public class UndoState {
    private final IMapObject target;
    private final IMapObject oldMapObject;
    private final IMapObject newMapObject;
    private final IMapObjectLayer layer;
    private final OperationType operationType;

    public UndoState(IMapObject target, OperationType operationType) {
      this.target = target;
      this.layer = Game.getEnvironment().getMap().getMapObjectLayer(target);
      this.oldMapObject = null;
      this.newMapObject = null;
      this.operationType = operationType;
    }

    public UndoState(IMapObject target, IMapObject oldMapObject, IMapObject newMapObject, OperationType operationType) {

      this.target = target;
      this.oldMapObject = operationType != OperationType.ADD ? oldMapObject : null;
      this.newMapObject = operationType != OperationType.DELETE ? newMapObject : null;
      this.operationType = operationType;

      this.layer = Game.getEnvironment().getMap().getMapObjectLayer(target);
    }

    @Override
    public String toString() {
      return target.getName() + "(" + target.getId() + ") " + this.operationType.toString();
    }
  }
}
