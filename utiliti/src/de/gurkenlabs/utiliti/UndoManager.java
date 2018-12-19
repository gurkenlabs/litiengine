package de.gurkenlabs.utiliti;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.utiliti.components.MapComponent;

public class UndoManager {
  private static final Logger log = Logger.getLogger(MapComponent.class.getName());
  private static final int MAX_STACK_SIZE = 10000;
  private static int nextOperation = 1;
  private UndoState[] undoStack;
  private List<IMapObject> changing;
  private int currentIndex = -1;
  private final String mapName;
  private int operation = 0;
  private boolean saved = true;
  private boolean executing;

  private static HashMap<String, UndoManager> instance;
  private static List<Consumer<UndoManager>> undoStackChangedConsumers;
  private static List<Consumer<UndoManager>> mapObjectAdded;
  private static List<Consumer<UndoManager>> mapObjectRemoved;

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

  public static UndoManager instance() {
    if (instance.containsKey(Game.world().environment().getMap().getName())) {
      return instance.get(Game.world().environment().getMap().getName());
    }

    UndoManager newUndoManager = new UndoManager(Game.world().environment().getMap().getName());
    instance.put(Game.world().environment().getMap().getName(), newUndoManager);
    return newUndoManager;
  }

  public static void clearAll() {
    instance.clear();
  }

  public void beginOperation() {
    this.operation = nextOperation;
  }

  public void endOperation() {
    ++nextOperation;
    this.operation = 0;
  }

  public void undo() {
    if (this.executing || this.currentIndex < 0) {
      return;
    }

    final int currentOperation = this.undoStack[this.currentIndex].getOperation();
    UndoState state = null;

    int stepsUndone = 0;
    this.executing = true;
    try {
      do {
        stepsUndone++;
        state = this.undoStack[this.currentIndex];
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
      } while (currentOperation != 0 && this.currentIndex >= 0 && this.undoStack[this.currentIndex].getOperation() == currentOperation);

      log.log(Level.FINE, "{0} steps undone.", stepsUndone);
      fireUndoStackChangedEvent(this);
    } finally {
      this.executing = false;
    }
  }

  public void redo() {
    if (this.executing || this.undoStack.length - 1 == this.currentIndex || this.undoStack[this.currentIndex + 1] == null) {
      return;
    }

    if (this.currentIndex >= this.undoStack.length - 1) {
      this.currentIndex = this.undoStack.length - 1;
      return;
    }

    final int currentOperation = this.undoStack[this.currentIndex + 1].getOperation();
    UndoState state = null;
    int stepsRedone = 0;
    this.executing = true;
    try {
      do {
        ++stepsRedone;
        ++this.currentIndex;
        state = this.undoStack[this.currentIndex];

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
      } while (currentOperation != 0 && this.currentIndex < MAX_STACK_SIZE && this.undoStack[this.currentIndex + 1] != null && this.undoStack[this.currentIndex + 1].getOperation() == currentOperation);

      log.log(Level.FINE, "{0} steps redone.", stepsRedone);
      fireUndoStackChangedEvent(this);
    } finally {
      this.executing = false;
    }
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
    if (executing || mapObject == null) {
      return;
    }

    if (this.changing.contains(mapObject)) {
      // the old state is already tracked, while multiple changes are carried
      // out, we
      // don't want to track the steps in between
      return;
    }

    this.changing.add(new MapObject((MapObject)mapObject,true));
  }

  public void mapObjectChanged(IMapObject mapObject) {
    if (executing || mapObject == null) {
      return;
    }

    Optional<IMapObject> trackedMapObject = this.changing.stream().filter(x -> x.getId() == mapObject.getId()).findFirst();
    if (!trackedMapObject.isPresent()) {
      // didn't track the changing event and therefore cannot provide an undo
      return;
    }

    this.ensureStackSize();

    this.currentIndex++;
    this.clearRedoSteps();

    this.undoStack[this.currentIndex] = new UndoState(mapObject, this.changing.remove(this.changing.indexOf(trackedMapObject.get())), new MapObject((MapObject)mapObject,true), OperationType.CHANGE);
    fireUndoStackChangedEvent(this);
  }

  public void mapObjectDeleted(IMapObject mapObject) {
    if (executing || mapObject == null) {
      return;
    }

    this.ensureStackSize();
    this.currentIndex++;
    this.clearRedoSteps();

    this.undoStack[this.currentIndex] = new UndoState(mapObject, OperationType.DELETE);
    fireUndoStackChangedEvent(this);
    fireUndoManagerEvent(mapObjectRemoved, this);
  }

  public void mapObjectAdded(IMapObject mapObject) {
    if (executing || mapObject == null) {
      return;
    }

    this.ensureStackSize();
    this.currentIndex++;
    this.clearRedoSteps();

    this.undoStack[this.currentIndex] = new UndoState(mapObject, OperationType.ADD);
    fireUndoStackChangedEvent(this);
    fireUndoManagerEvent(mapObjectAdded, this);
  }

  public static void onUndoStackChanged(Consumer<UndoManager> cons) {
    undoStackChangedConsumers.add(cons);
  }

  public static void onMapObjectAdded(Consumer<UndoManager> cons) {
    mapObjectAdded.add(cons);
  }

  public static void onMapObjectRemoved(Consumer<UndoManager> cons) {
    mapObjectRemoved.add(cons);
  }

  public static boolean hasChanges(IMap map) {
    if (instance.containsKey(map.getName())) {
      return !instance.get(map.getName()).saved;
    }

    return false;
  }

  static void save(IMap map) {
    if (instance.containsKey(map.getName())) {
      instance.get(map.getName()).saved = true;
    }
  }

  private static final void fireUndoStackChangedEvent(UndoManager undoManager) {
    undoManager.saved = false;
    fireUndoManagerEvent(undoStackChangedConsumers, undoManager);
  }

  private static final void fireUndoManagerEvent(List<Consumer<UndoManager>> consumers, UndoManager undoManager) {
    for (Consumer<UndoManager> cons : consumers) {
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
    target.getProperties().clear();
    for (Map.Entry<String, ICustomProperty> prop : restore.getProperties().entrySet()) {
      target.setValue(prop.getKey(), prop.getValue());
    }

    Game.world().environment().reloadFromMap(target.getId());
    if (MapObjectType.get(target.getType()) == MapObjectType.LIGHTSOURCE) {
      Game.world().environment().getAmbientLight().updateSection(MapObject.getBounds2D((MapObject) target, (MapObject) restore));
    }

    if (EditorScreen.instance().getMapComponent().getFocusedMapObject() != null && EditorScreen.instance().getMapComponent().getFocusedMapObject().getId() == target.getId()) {
      EditorScreen.instance().getMapObjectPanel().bind(target);
      EditorScreen.instance().getMapSelectionPanel().focus(target);
    }
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

  private void clearRedoSteps() {
    // whenever a new UndoState gets added, while we're in the middle of the
    // current
    // stack, we need to remove all future redo steps because the new state will
    // now
    // be the last element
    int index = this.currentIndex + 1;
    while (this.undoStack[index] != null && index < MAX_STACK_SIZE) {
      this.undoStack[index] = null;
      index++;
    }
  }

  public String getMapName() {
    return mapName;
  }

  private enum OperationType {
    CHANGE, ADD, DELETE
  }

  public class UndoState {
    private final IMapObject target;
    private final IMapObject oldMapObject;
    private final IMapObject newMapObject;
    private final IMapObjectLayer layer;
    private final OperationType operationType;
    private final int operation;

    public UndoState(IMapObject target, OperationType operationType) {
      this.operation = UndoManager.this.operation;
      this.target = target;
      this.layer = Game.world().environment().getMap().getMapObjectLayer(target);
      this.oldMapObject = null;
      this.newMapObject = null;
      this.operationType = operationType;
    }

    public UndoState(IMapObject target, IMapObject oldMapObject, IMapObject newMapObject, OperationType operationType) {
      this.operation = UndoManager.this.operation;
      this.target = target;
      this.oldMapObject = operationType != OperationType.ADD ? oldMapObject : null;
      this.newMapObject = operationType != OperationType.DELETE ? newMapObject : null;
      this.operationType = operationType;

      this.layer = Game.world().environment().getMap().getMapObjectLayer(target);
    }

    @Override
    public String toString() {
      return target.getName() + "(" + target.getId() + ") " + this.operationType.toString();
    }

    public int getOperation() {
      return this.operation;
    }
  }
}
