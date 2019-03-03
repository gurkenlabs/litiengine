package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.RenderOrder;
import de.gurkenlabs.litiengine.resources.Resources;

public class MapTests {
  @BeforeEach
  public void clearResources() {
    Resources.maps().clear();
  }

  @Test
  public void testBasicProperties() {
    IMap map = Resources.maps().get("tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-map.tmx");

    assertEquals(1.0, map.getVersion());
    assertEquals("1.1.4", map.getTiledVersion());
    assertEquals(MapOrientations.ORTHOGONAL, map.getOrientation());
    assertEquals(RenderOrder.RIGHT_DOWN, map.getRenderOrder());
    assertEquals(256, map.getSizeInPixels().width);
    assertEquals(256, map.getSizeInPixels().height);
    assertEquals(16, map.getTileSize().width);
    assertEquals(16, map.getTileSize().height);
    assertEquals(16, map.getSizeInTiles().width);
    assertEquals(16, map.getSizeInTiles().height);
    assertEquals(1, map.getNextObjectId());

    assertEquals("test-map", map.getName());
    assertEquals("tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-map.tmx", map.getPath());
    assertEquals(new Color(200, 0, 0), map.getBackgroundColor());
    assertEquals(new Rectangle2D.Double(0, 0, 256, 256), map.getBounds());
    assertEquals(2, map.getTilesets().size());
    assertEquals(1, ((Map) map).getExternalTilesets().size());
    assertEquals("external-tileset", map.getTilesets().get(1).getName());
    assertEquals(1, map.getTileLayers().size());
    assertEquals(16, map.getTileLayers().get(0).getSizeInTiles().width);
    assertEquals(16, map.getTileLayers().get(0).getSizeInTiles().height);
    assertEquals(0, map.getImageLayers().size());
    assertEquals(1, map.getRenderLayers().size());
    assertEquals(0, map.getMapObjectLayers().size());
    assertEquals(0, map.getProperties().size());
    assertEquals(0, map.getMapObjects().size());
  }

  @Test
  public void testTileCustomProperties() {
    IMap map = Resources.maps().get("tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-map.tmx");

    assertEquals("bar", map.getTileLayers().get(0).getTile(5, 3).getStringValue("foo"));
    assertEquals("bap", map.getTileLayers().get(0).getTile(9, 5).getStringValue("baz"));
    assertEquals("multiline\nproperty", map.getTileLayers().get(0).getTile(10, 10).getStringValue("custom"));
  }

  @Test
  public void testSettingProperties() {
    Map map = (Map) Resources.maps().get("tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-map.tmx");
    map.setOrientation(MapOrientations.ISOMETRIC_STAGGERED);
    map.setTiledVersion("0.0.0");
    map.setVersion(2.0);
    map.setWidth(64);
    map.setHeight(64);

    map.setTileHeight(32);
    map.setTileWidth(32);
    map.setName("test");
    map.setRenderOrder(RenderOrder.RIGHT_UP);

    assertEquals(64, map.getSizeInTiles().width);
    assertEquals(64, map.getSizeInTiles().height);
    assertEquals(MapOrientations.ISOMETRIC_STAGGERED, map.getOrientation());
    assertEquals("0.0.0", map.getTiledVersion());
    assertEquals(2.0, map.getVersion());
    assertEquals(32, map.getTileSize().width);
    assertEquals(32, map.getTileSize().height);
    assertEquals("test", map.getName());
    assertEquals(RenderOrder.RIGHT_UP, map.getRenderOrder());
  }

  @Test
  public void testMapObjectLayers() {
    IMap map = Resources.maps().get("tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-mapobject.tmx");
    assertEquals(1, map.getMapObjectLayers().size());

    IMapObjectLayer layer = map.getMapObjectLayers().get(0);
    assertEquals("test", layer.getName());
    assertEquals(1, layer.getMapObjects().size());
    assertEquals(16, layer.getSizeInTiles().width);

    IMapObject object = map.getMapObject(1);

    assertEquals(layer, map.getMapObjectLayer(object));

    assertEquals(1, map.getMapObjects("TEST_TYPE").size());
    assertEquals(1, map.getMapObjects().size());
    assertEquals("TEST_TYPE", object.getType());
    assertEquals("bar", object.getStringValue("foo"));
    assertEquals(0.1f, object.getX());
    assertEquals(0.1f, object.getY());
    assertEquals(10.1f, object.getWidth());
    assertEquals(10.1f, object.getHeight());
    assertEquals("bar", object.getStringValue("foo"));

    map.addLayer(mock(MapObjectLayer.class));
    assertEquals(2, map.getMapObjectLayers().size());

    map.removeMapObject(1);

    assertNull(map.getMapObject(1));

    map.removeLayer(layer);

    assertEquals(1, map.getMapObjectLayers().size());

    map.removeLayer(1);

    assertEquals(0, map.getMapObjectLayers().size());
  }

  @Test
  public void testDecimalFloatAdapter() throws Exception {
    DecimalFloatAdapter adapter = new DecimalFloatAdapter();
    assertEquals("1", adapter.marshal(1f));
    assertEquals("1", adapter.marshal(1.0f));
    assertEquals("1", adapter.marshal(1.00f));
    assertEquals("1.1", adapter.marshal(1.1f));
    assertEquals("1.00003", adapter.marshal(1.00003f));
  }

  @Test
  public void testInfiniteMap() {
    Map map = (Map) Resources.maps().get("tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-infinite-map.tmx");

    assertTrue(map.isInfinite());
    assertEquals(64, map.getWidth());
    assertEquals(64, map.getHeight());
    assertEquals(2, map.getTileLayers().size());
    assertEquals(2, map.getTileLayers().size());

    assertEquals(1, map.getTileLayers().get(0).getTile(15, 24).getGridId());
  }
}
