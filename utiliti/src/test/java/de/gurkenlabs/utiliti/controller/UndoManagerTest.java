package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.GroupLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.ImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapImage;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class UndoManagerTest {

  @AfterEach
  void cleanup() throws Exception {
    UndoManager.clearAll();
    Method terminate = Game.class.getDeclaredMethod("terminate");
    terminate.setAccessible(true);
    terminate.invoke(null);
  }

  @Test
  void restoreStateMovesMapObjectBackToSnapshotLayer() throws Exception {
    MapObjectLayer sourceLayer = new MapObjectLayer();
    MapObjectLayer targetLayer = new MapObjectLayer();
    MapObject target = new MapObject();
    target.setId(1);
    target.setName("target");
    targetLayer.addMapObject(target);

    MapObject snapshot = new MapObject();
    snapshot.setId(1);
    snapshot.setName("snapshot");
    setSnapshotLayer(snapshot, sourceLayer);

    restoreState(target, snapshot);

    assertSame(sourceLayer, target.getLayer());
    assertTrue(sourceLayer.getMapObjects().contains(target));
    assertFalse(targetLayer.getMapObjects().contains(target));
    assertEquals("snapshot", target.getName());
  }

  @Test
  void restoreStateRemovesMapObjectFromLayerWhenSnapshotHasNoLayer() throws Exception {
    MapObjectLayer targetLayer = new MapObjectLayer();
    MapObject target = new MapObject();
    target.setId(1);
    targetLayer.addMapObject(target);

    MapObject snapshot = new MapObject();
    snapshot.setId(1);

    restoreState(target, snapshot);

    assertNull(target.getLayer());
    assertFalse(targetLayer.getMapObjects().contains(target));
  }

  @Test
  void hasChangesReturnsFalseForNullMap() {
    assertFalse(UndoManager.hasChanges(null));
  }

  @Test
  void recordsMapObjectChangesForInactiveMap() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("inactive-map");
    MapObjectLayer layer = new MapObjectLayer();
    MapObject object = new MapObject();
    object.setId(42);
    layer.addMapObject(object);
    map.addLayer(layer);
    UndoManager manager = UndoManager.forMap(map);

    manager.mapObjectChanging(object);
    object.setX(12);
    manager.mapObjectChanged(object);

    assertTrue(manager.canUndo());
    manager.undo();
    assertEquals(0, object.getX());
  }

  @Test
  void failingStackListenerDoesNotBlockOtherListenersOrRecording() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    UndoManager manager = UndoManager.forMap(newMap("listener-isolation"));
    AtomicInteger events = new AtomicInteger();
    Consumer<UndoManager> failing = ignored -> {
      throw new IllegalStateException("listener failure");
    };
    Consumer<UndoManager> succeeding = ignored -> events.incrementAndGet();
    UndoManager.onUndoStackChanged(failing);
    UndoManager.onUndoStackChanged(succeeding);

    try {
      manager.resourceChanged(() -> {}, () -> {});

      assertTrue(manager.canUndo());
      assertEquals(1, events.get());
    } finally {
      UndoManager.removeUndoStackChanged(failing);
      UndoManager.removeUndoStackChanged(succeeding);
    }
  }

  @Test
  void undoManagerIdentitySurvivesMapRename() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("before");
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();
    manager.recordChanges();

    map.setName("after");

    assertSame(manager, UndoManager.instance());
    assertTrue(UndoManager.hasChanges(map));
    assertEquals("after", manager.getMapName());
  }

  @Test
  void mapUndoRestoresLiveAmbientLight() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("undo-live-ambient-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    map.setValue(MapProperty.AMBIENTCOLOR, Color.BLACK);
    Game.world().loadEnvironment(map);

    UndoManager manager = UndoManager.instance();
    manager.mapChanging(map);
    map.setValue(MapProperty.AMBIENTCOLOR, Color.WHITE);
    Game.world().environment().getAmbientLight().setColor(Color.WHITE);
    manager.mapChanged(map);

    manager.undo();

    assertEquals(Color.BLACK, map.getColorValue(MapProperty.AMBIENTCOLOR));
    assertEquals(Color.BLACK, Game.world().environment().getAmbientLight().getColor());
  }

  @Test
  void mapUndoRedoRestoresTilesetMembership() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("undo-map-tilesets-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    Tileset first = new Tileset();
    first.setName("first");
    Tileset second = new Tileset();
    second.setName("second");
    map.getTilesets().add(first);
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();

    manager.mapChanging(map);
    map.getTilesets().add(second);
    manager.mapChanged(map);
    manager.undo();

    assertEquals(1, map.getTilesets().size());
    assertSame(first, map.getTilesets().getFirst());
    manager.redo();
    assertEquals(2, map.getTilesets().size());
    assertSame(second, map.getTilesets().get(1));
  }

  @Test
  void layerUndoRestoresLayerProperties() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("undo-layer-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    MapObjectLayer layer = new MapObjectLayer();
    layer.setName("before");
    layer.setValue("custom", "old");
    map.addLayer(layer);
    Game.world().loadEnvironment(map);

    UndoManager manager = UndoManager.instance();
    manager.layerChanging(layer);
    layer.setName("after");
    layer.setValue("custom", "new");
    manager.layerChanged(layer);

    manager.undo();

    assertEquals("before", layer.getName());
    assertEquals("old", layer.getStringValue("custom"));
  }

  @Test
  void layerUndoRedoPreservesCustomPropertyTypes() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("undo-layer-property-types-test");
    MapObjectLayer layer = new MapObjectLayer();
    setTypedProperties(layer);
    map.addLayer(layer);
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();

    manager.layerChanging(layer);
    layer.setOpacity(0.5f);
    manager.layerChanged(layer);

    manager.undo();
    assertTypedProperties(layer);
    manager.redo();
    assertTypedProperties(layer);
  }

  @Test
  void mapUndoRedoPreservesCustomPropertyTypes() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("undo-map-property-types-test");
    setTypedProperties(map);
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();

    manager.mapChanging(map);
    map.setValue("changed", "after");
    manager.mapChanged(map);

    manager.undo();
    assertTypedProperties(map);
    manager.redo();
    assertTypedProperties(map);
  }

  @Test
  void layerStructureUndoRedoRestoresLayerOrder() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("undo-layer-structure-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    MapObjectLayer first = new MapObjectLayer();
    first.setName("first");
    MapObjectLayer second = new MapObjectLayer();
    second.setName("second");
    map.addLayer(first);
    map.addLayer(second);
    Game.world().loadEnvironment(map);

    UndoManager manager = UndoManager.instance();
    manager.layerStructureChanging(map);
    map.removeLayer(second);
    map.addLayer(0, second);
    manager.layerStructureChanged(map);

    assertSame(second, map.getRenderLayers().get(0));
    manager.undo();
    assertSame(first, map.getRenderLayers().get(0));
    assertSame(second, map.getRenderLayers().get(1));
    manager.redo();
    assertSame(second, map.getRenderLayers().get(0));
    assertSame(first, map.getRenderLayers().get(1));
  }

  @Test
  void layerStructureUndoRestoresNestedGroupOrder() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("undo-nested-layer-structure-test");
    GroupLayer group = new GroupLayer();
    MapObjectLayer first = new MapObjectLayer();
    MapObjectLayer second = new MapObjectLayer();
    group.addLayer(first);
    group.addLayer(second);
    map.addLayer(group);
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();

    manager.layerStructureChanging(map);
    group.removeLayer(second);
    group.addLayer(0, second);
    manager.layerStructureChanged(map);

    manager.undo();
    assertSame(first, group.getRenderLayers().get(0));
    assertSame(second, group.getRenderLayers().get(1));
    manager.redo();
    assertSame(second, group.getRenderLayers().get(0));
  }

  @Test
  void originatingManagerRestoresInactiveMapWithoutTouchingCurrentEnvironment() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap origin = newMap("origin-map");
    MapObjectLayer originLayer = new MapObjectLayer();
    origin.addLayer(originLayer);
    Game.world().loadEnvironment(origin);
    UndoManager originManager = UndoManager.forMap(origin);
    originManager.layerStructureChanging(origin);
    origin.removeLayer(originLayer);
    originManager.layerStructureChanged(origin);

    TmxMap current = newMap("current-map");
    MapObjectLayer currentLayer = new MapObjectLayer();
    current.addLayer(currentLayer);
    Game.world().loadEnvironment(current);
    originManager.undo();

    assertSame(current, Game.world().environment().getMap());
    assertSame(currentLayer, current.getRenderLayers().getFirst());
    assertSame(originLayer, origin.getRenderLayers().getFirst());
  }

  @Test
  void recursiveLayerPropertySnapshotRestoresAllVisibilityChangesAsOneStep() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("undo-layer-visibility-test");
    GroupLayer group = new GroupLayer();
    MapObjectLayer child = new MapObjectLayer();
    MapObjectLayer sibling = new MapObjectLayer();
    group.addLayer(child);
    map.addLayer(group);
    map.addLayer(sibling);
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();
    int historySize = manager.getUndoHistory().size();

    manager.layersChanging(map);
    group.setVisible(false);
    child.setVisible(false);
    sibling.setVisible(false);
    manager.layersChanged(map);

    assertEquals(historySize + 1, manager.getUndoHistory().size());
    manager.undo();
    assertTrue(group.isVisible());
    assertTrue(child.isVisible());
    assertTrue(sibling.isVisible());
  }

  @Test
  void recursiveLayerPropertySnapshotDetectsRemovedLayers() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("removed-layer-properties-test");
    MapObjectLayer first = new MapObjectLayer();
    MapObjectLayer removed = new MapObjectLayer();
    map.addLayer(first);
    map.addLayer(removed);
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();
    int historySize = manager.getUndoHistory().size();

    manager.layersChanging(map);
    map.removeLayer(removed);
    manager.layersChanged(map);

    assertEquals(historySize + 1, manager.getUndoHistory().size());
  }

  @Test
  void layerUndoRestoresCompleteImageAndDiscardsNoOpSnapshots() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("undo-image-layer-test");
    ImageLayer layer = new ImageLayer();
    MapImage image = new MapImage();
    image.setSource("before.png");
    image.setAbsoluteSourcePath(new URL("file:/maps/before.png"));
    image.setTransparentColor(Color.MAGENTA);
    image.setWidth(32);
    image.setHeight(64);
    image.setValue("custom", "before");
    layer.setImage(image);
    map.addLayer(layer);
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();
    int initialHistorySize = manager.getUndoHistory().size();

    manager.layerChanging(layer);
    manager.layerChanged(layer);
    assertEquals(initialHistorySize, manager.getUndoHistory().size());

    manager.layerChanging(layer);
    image.setSource("after.png");
    image.setAbsoluteSourcePath(new URL("file:/maps/after.png"));
    image.setTransparentColor(Color.GREEN);
    image.setWidth(1);
    image.setValue("custom", "after");
    manager.layerChanged(layer);
    manager.undo();
    assertNotSame(image, layer.getImage());
    assertEquals("before.png", layer.getImage().getSource());
    assertEquals(new URL("file:/maps/before.png"), layer.getImage().getAbsoluteSourcePath());
    assertEquals(Color.MAGENTA, layer.getImage().getTransparentColor());
    assertEquals(32, layer.getImage().getWidth());
    assertEquals(64, layer.getImage().getHeight());
    assertEquals("before", layer.getImage().getStringValue("custom"));
  }

  @Test
  void undoingBackToSavedRevisionClearsDirtyState() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("saved-revision-test");
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();
    AtomicInteger value = new AtomicInteger();
    manager.resourceChanged(() -> value.set(0), () -> value.set(1));
    UndoManager.save(map);
    manager.resourceChanged(() -> value.set(1), () -> value.set(2));
    assertTrue(UndoManager.hasChanges(map));

    manager.undo();

    assertFalse(UndoManager.hasChanges(map));
    manager.redo();
    assertTrue(UndoManager.hasChanges(map));
  }

  @Test
  void targetedUndoDoesNotUndoANewerEdit() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("targeted-undo-test");
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();
    AtomicInteger value = new AtomicInteger(1);
    manager.resourceChanged(() -> value.set(0), () -> value.set(1));
    long deletionRevision = manager.getRevision();
    value.set(2);
    manager.resourceChanged(() -> value.set(1), () -> value.set(2));

    assertFalse(manager.undoIfRevision(deletionRevision));
    assertEquals(2, value.get());
    assertEquals(2, manager.getUndoHistory().size());
  }

  @Test
  void targetedUndoUndoesMatchingHistoryHead() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("matching-targeted-undo-test");
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();
    AtomicInteger value = new AtomicInteger(1);
    manager.resourceChanged(() -> value.set(0), () -> value.set(1));

    assertTrue(manager.undoIfRevision(manager.getRevision()));
    assertEquals(0, value.get());
  }

  @Test
  void evictingInitialSavedBoundaryKeepsMapDirtyAfterAllAvailableUndos() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("saved-boundary-eviction-test");
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();
    for (int i = 0; i <= 10000; i++) {
      manager.resourceChanged(() -> {}, () -> {});
    }

    while (manager.canUndo()) {
      manager.undo();
    }

    assertTrue(UndoManager.hasChanges(map));
  }

  @Test
  void renameConflictDoesNotConsumeOrPartiallyApplyUndo() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap renamed = newMap("rename-old");
    TmxMap other = newMap("rename-other");
    MapComponent mapComponent = Editor.instance().getMapComponent();
    mapComponent.getMaps().clear();
    mapComponent.getMaps().add(renamed);
    mapComponent.getMaps().add(other);
    Game.world().loadEnvironment(renamed);
    UndoManager manager = UndoManager.forMap(renamed);
    manager.mapChanging(renamed);
    renamed.setName("rename-new");
    renamed.setValue("custom", "changed");
    manager.mapChanged(renamed);
    other.setName("rename-old");

    manager.undo();

    assertTrue(manager.canUndo());
    assertEquals("rename-new", renamed.getName());
    assertEquals("changed", renamed.getStringValue("custom"));
    mapComponent.getMaps().clear();
  }

  @Test
  void mapSnapshotsDiscardNoOpChanges() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("undo-map-no-op-test");
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();
    int initialHistorySize = manager.getUndoHistory().size();

    manager.mapChanging(map);
    manager.mapChanged(map);

    assertEquals(initialHistorySize, manager.getUndoHistory().size());
  }

  @Test
  void resourceUndoRedoExecutesResourceSnapshots() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("undo-resource-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    Game.world().loadEnvironment(map);
    AtomicInteger value = new AtomicInteger(1);

    UndoManager manager = UndoManager.instance();
    value.set(2);
    manager.resourceChanged(() -> value.set(1), () -> value.set(2));

    manager.undo();
    assertEquals(1, value.get());
    manager.redo();
    assertEquals(2, value.get());
  }

  @Test
  void historyGroupsMultiStepOperationsForToolbarMenus() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("undo-history-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();
    AtomicInteger value = new AtomicInteger();

    manager.beginOperation();
    manager.resourceChanged(() -> value.set(0), () -> value.set(1));
    manager.resourceChanged(() -> value.set(1), () -> value.set(2));
    manager.endOperation();

    assertEquals(1, manager.getUndoHistory().size());
    assertEquals(2, manager.getUndoHistory().getFirst().steps());
    assertEquals("2 changes", manager.getUndoHistory().getFirst().description());
    manager.undo();
    assertEquals(1, manager.getRedoHistory().size());
    assertEquals(2, manager.getRedoHistory().getFirst().steps());
  }

  @Test
  void historyDescribesGroupedObjectOperationsWithoutNamingOneObject() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("undo-group-description-test");
    MapObjectLayer layer = new MapObjectLayer();
    MapObject first = new MapObject();
    first.setId(20);
    first.setType("COLLISIONBOX");
    MapObject second = new MapObject();
    second.setId(21);
    second.setType("COLLISIONBOX");
    layer.addMapObject(first);
    layer.addMapObject(second);
    map.addLayer(layer);
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();

    manager.beginOperation();
    manager.mapObjectChanging(first);
    first.setX(10);
    manager.mapObjectMoved(first);
    manager.mapObjectChanging(second);
    second.setX(20);
    manager.mapObjectMoved(second);
    manager.endOperation();

    assertEquals("Move 2 objects", manager.getUndoHistory().getFirst().description());
    manager.undo();
    assertEquals("Move 2 objects", manager.getRedoHistory().getFirst().description());
  }

  @Test
  void groupedOperationEmitsOneStackChange() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("undo-event-aggregation-test");
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();
    AtomicInteger events = new AtomicInteger();
    Consumer<UndoManager> listener = ignored -> events.incrementAndGet();
    UndoManager.onUndoStackChanged(listener);

    try {
      manager.beginOperation();
      manager.resourceChanged(() -> {}, () -> {});
      manager.resourceChanged(() -> {}, () -> {});

      assertEquals(0, events.get());
      manager.endOperation();
      assertEquals(1, events.get());
      assertEquals(2, manager.getRevision());
    } finally {
      UndoManager.removeUndoStackChanged(listener);
    }
  }

  @Test
  void bulkUndoAndRedoEmitOneStackChangeEach() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = newMap("undo-bulk-event-aggregation-test");
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();

    manager.resourceChanged(() -> {}, () -> {});
    manager.resourceChanged(() -> {}, () -> {});
    long recordedRevision = manager.getRevision();
    AtomicInteger events = new AtomicInteger();
    Consumer<UndoManager> listener = ignored -> events.incrementAndGet();
    UndoManager.onUndoStackChanged(listener);

    try {
      manager.undo(2);
      assertEquals(recordedRevision + 2, manager.getRevision());
      assertEquals(1, events.get());
      assertFalse(manager.canUndo());

      manager.redo(2);
      assertEquals(recordedRevision + 4, manager.getRevision());
      assertEquals(2, events.get());
      assertFalse(manager.canRedo());
    } finally {
      UndoManager.removeUndoStackChanged(listener);
    }
  }

  @Test
  void historyDescribesMapObjectTransforms() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("undo-transform-history-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    MapObjectLayer layer = new MapObjectLayer();
    MapObject object = new MapObject();
    object.setId(1);
    object.setType("PROP");
    layer.addMapObject(object);
    map.addLayer(layer);
    Game.world().loadEnvironment(map);
    UndoManager manager = UndoManager.instance();

    manager.mapObjectChanging(object);
    object.setX(10);
    manager.mapObjectMoved(object);

    assertEquals("Move PROP #1", manager.getUndoHistory().getFirst().description());

    manager.mapObjectChanging(object);
    object.setWidth(32);
    manager.mapObjectResized(object);

    assertEquals("Resize PROP #1", manager.getUndoHistory().getFirst().description());
  }

  private static void restoreState(IMapObject target, IMapObject snapshot) throws Exception {
    Method restoreState = UndoManager.class.getDeclaredMethod("restoreState", IMapObject.class, IMapObject.class);
    restoreState.setAccessible(true);
    restoreState.invoke(null, target, snapshot);
  }

  private static void setSnapshotLayer(MapObject object, MapObjectLayer layer) throws Exception {
    Field layerField = MapObject.class.getDeclaredField("layer");
    layerField.setAccessible(true);
    layerField.set(object, layer);
  }

  private static void setTypedProperties(ICustomPropertyProvider provider) throws Exception {
    MapObject referencedObject = new MapObject();
    referencedObject.setId(42);
    provider.setValue("int", 5);
    provider.setValue("float", 1.5f);
    provider.setValue("bool", true);
    provider.setValue("color", Color.BLUE);
    provider.setValue("file", new URL("file:/maps/property.txt"));
    provider.setValue("object", referencedObject);
  }

  private static void assertTypedProperties(ICustomPropertyProvider provider) {
    assertEquals("int", provider.getProperty("int").getType());
    assertEquals("float", provider.getProperty("float").getType());
    assertEquals("bool", provider.getProperty("bool").getType());
    assertEquals("color", provider.getProperty("color").getType());
    assertEquals("file", provider.getProperty("file").getType());
    assertEquals("object", provider.getProperty("object").getType());
  }

  private static TmxMap newMap(String name) {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName(name);
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    return map;
  }
}
