package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
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
    assertEquals(1, map.getNextObjectId());

    assertEquals("test-map", map.getName());
    assertEquals(new Color(200, 0, 0), map.getBackgroundColor());
    assertEquals(new Rectangle2D.Double(0, 0, 16, 16), map.getBounds());
    assertEquals(1, map.getTilesets().size());
    assertEquals(1, map.getTileLayers().size());
  }
}
