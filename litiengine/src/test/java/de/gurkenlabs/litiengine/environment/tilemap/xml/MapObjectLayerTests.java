package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class MapObjectLayerTests {

  @Test
  void addMapObjectAtIndexPreservesOrderAndLayer() {
    MapObjectLayer layer = new MapObjectLayer();
    MapObject first = new MapObject();
    MapObject second = new MapObject();
    MapObject inserted = new MapObject();

    layer.addMapObject(first);
    layer.addMapObject(second);
    layer.addMapObject(1, inserted);

    assertEquals(java.util.List.of(first, inserted, second), layer.getMapObjects());
    assertSame(layer, inserted.getLayer());
  }
}
