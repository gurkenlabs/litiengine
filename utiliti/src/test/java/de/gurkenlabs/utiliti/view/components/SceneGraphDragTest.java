package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SceneGraphDragTest {
  @AfterEach
  void cleanup() throws Exception {
    Method terminate = Game.class.getDeclaredMethod("terminate");
    terminate.setAccessible(true);
    terminate.invoke(null);
  }

  @Test
  void movesMapObjectsAsOrderedBlock() {
    TmxMap map = createMap("object-block-move");
    MapObjectLayer layer = layer("objects");
    MapObject first = object(1);
    MapObject second = object(2);
    MapObject third = object(3);
    MapObject fourth = object(4);
    layer.addMapObject(first);
    layer.addMapObject(second);
    layer.addMapObject(third);
    layer.addMapObject(fourth);
    map.addLayer(layer);
    Game.world().loadEnvironment(map);

    assertTrue(SceneGraph.moveMapObjectsForTest(List.of(second, third), layer, 4));

    assertEquals(List.of(first, fourth, second, third), layer.getMapObjects());
  }

  @Test
  void movesLayersAsOrderedVisualBlock() {
    TmxMap map = createMap("layer-block-move");
    MapObjectLayer first = layer("first");
    MapObjectLayer second = layer("second");
    MapObjectLayer third = layer("third");
    MapObjectLayer fourth = layer("fourth");
    map.addLayer(first);
    map.addLayer(second);
    map.addLayer(third);
    map.addLayer(fourth);
    Game.world().loadEnvironment(map);

    assertTrue(SceneGraph.moveLayersForTest(List.of(fourth, third), first));

    assertEquals(List.of(first, third, fourth, second), map.getRenderLayers());
  }

  private static TmxMap createMap(String name) {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName(name);
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    return map;
  }

  private static MapObjectLayer layer(String name) {
    MapObjectLayer layer = new MapObjectLayer();
    layer.setName(name);
    return layer;
  }

  private static MapObject object(int id) {
    MapObject object = new MapObject();
    object.setId(id);
    object.setType("AREA");
    return object;
  }
}
