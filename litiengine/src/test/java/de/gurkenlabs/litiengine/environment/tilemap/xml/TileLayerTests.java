package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

class TileLayerTests {
  @Test
  void createsEditableEmptyTiles() {
    TileLayer layer = new TileLayer(3, 2);

    assertEquals(0, layer.getTile(0, 0).getGridId());
    assertEquals(0, layer.getTile(2, 1).getGridId());

    layer.setTile(2, 1, 17);

    assertEquals(17, layer.getTile(2, 1).getGridId());
  }

  @Test
  void parsesIndependentEmptyTiles() throws InvalidTileLayerException {
    var tiles = TileData.parseCsvData("0,0");

    assertNotSame(tiles.get(0), tiles.get(1));
  }
}
