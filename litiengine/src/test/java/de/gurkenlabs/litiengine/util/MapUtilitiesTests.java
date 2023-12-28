package de.gurkenlabs.litiengine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class MapUtilitiesTests {
  @Test
  void testGetBounds() {
    IMapObject object1 = mock(IMapObject.class);
    when(object1.getBoundingBox()).thenReturn(new Rectangle2D.Double(0, 0, 10, 10));

    IMapObject object2 = mock(IMapObject.class);
    when(object2.getBoundingBox()).thenReturn(new Rectangle2D.Double(5, 5, 15, 15));

    Rectangle2D result = MapUtilities.getBounds(object1, object2);

    assertEquals(0, result.getX());
    assertEquals(0, result.getY());
    assertEquals(20, result.getWidth());
    assertEquals(20, result.getHeight());
  }

  @Test
  void testGetMaxMapId() {
    IMapObject object1 = mock(IMapObject.class);
    when(object1.getId()).thenReturn(1);

    IMapObject object2 = mock(IMapObject.class);
    when(object2.getId()).thenReturn(3);

    IMapObjectLayer layer1 = mock(IMapObjectLayer.class);
    when(layer1.getMapObjects()).thenReturn(Arrays.asList(object1, object2));

    IMapObjectLayer layer2 = mock(IMapObjectLayer.class);
    when(layer2.getMapObjects()).thenReturn(List.of());

    IMap map = mock(IMap.class);
    when(map.getMapObjectLayers()).thenReturn(Arrays.asList(layer1, layer2));

    assertEquals(3, MapUtilities.getMaxMapId(map));
  }

  @Test
  void testGetTileBoundingBox() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
    when(map.getWidth()).thenReturn(10);
    when(map.getHeight()).thenReturn(10);
    when(map.getTileWidth()).thenReturn(10);
    when(map.getTileHeight()).thenReturn(10);
    when(map.getTileSize()).thenReturn(new Dimension(10, 10));
    when(map.getOrientation()).thenReturn(MapOrientations.ORTHOGONAL);

    Rectangle2D box = new Rectangle2D.Double(-10, -10, 25, 25);

    Rectangle2D result = MapUtilities.getTileBoundingBox(map, box);

    assertEquals(0, result.getX());
    assertEquals(0, result.getY());
    assertEquals(20, result.getWidth());
    assertEquals(20, result.getHeight());
  }

}
