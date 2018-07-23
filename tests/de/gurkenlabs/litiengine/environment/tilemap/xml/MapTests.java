package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.geom.Rectangle2D;


import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapLoader;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientation;


public class MapTests {

  @Test
  public void testBasicProperties() {
    IMap map = MapLoader.load("tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-map.tmx");

    assertEquals(1.0, map.getVersion());
    assertEquals("1.1.4", map.getTiledVersion());
    assertEquals(MapOrientation.ORTHOGONAL, map.getOrientation());
    assertEquals("right-down", map.getRenderorder());
    assertEquals(16, map.getSizeInPixels().width);
    assertEquals(16, map.getSizeInPixels().height);
    assertEquals(1, map.getTileSize().width);
    assertEquals(1, map.getTileSize().height);
    assertEquals(16, map.getSizeInTiles().width);
    assertEquals(16, map.getSizeInTiles().height);
    assertEquals(1, map.getNextObjectId());

    assertEquals("test-map", map.getName());
    assertEquals(new Color(200, 0, 0), map.getBackgroundColor());
    assertEquals(new Rectangle2D.Double(0, 0, 16, 16), map.getBounds());
    assertEquals(2, map.getTilesets().size());
    assertEquals("tiles-test", map.getTilesets().get(1).getName());
    assertEquals(1, map.getTileLayers().size());
    assertEquals(0, map.getImageLayers().size());
    assertEquals(1, map.getRenderLayers().size());
    assertEquals(0, map.getMapObjectLayers().size());
    assertEquals(0, map.getCustomProperties().size());
    assertEquals(0, map.getMapObjects().size());
  }

  @Test
  public void testMapObjectLayers() {
    IMap map = MapLoader.load("tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-mapobject.tmx");
    assertEquals(1, map.getMapObjectLayers().size());

    IMapObjectLayer layer = map.getMapObjectLayers().get(0);
    assertEquals("test", layer.getName());
    assertEquals(1, layer.getMapObjects().size());
    
    IMapObject object = map.getMapObject(1);
    assertEquals("TEST_TYPE", object.getType());
    assertEquals("bar", object.getCustomProperty("foo"));
    assertEquals(0.1f, object.getX());
    assertEquals(0.1f, object.getY());
    assertEquals(10.1f, object.getWidth());
    assertEquals(10.1f, object.getHeight());
    assertEquals("bar", object.getCustomProperty("foo"));

    map.addMapObjectLayer(mock(MapObjectLayer.class));
    assertEquals(2, map.getMapObjectLayers().size());
  }
}
