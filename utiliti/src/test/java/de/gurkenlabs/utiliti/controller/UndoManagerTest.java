package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
}
