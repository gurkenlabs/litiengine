package de.gurkenlabs.litiengine.environment.tilemap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import java.nio.file.Files;
import java.nio.file.Path;
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
  void copiedLayerUsesOneDeeplyIsolatedTileGraph() {
    TileLayer original = new TileLayer(2, 1);
    original.setTile(0, 0, 4);
    TileLayer copy = new TileLayer(original);

    assertSame(copy.getTile(0, 0), copy.getTiles().getFirst());
    assertSame(copy.getTile(0, 0), copy.getRawTileData().getTiles().getFirst());
    assertNotSame(original.getTile(0, 0), copy.getTile(0, 0));

    copy.setTile(0, 0, 9);
    assertEquals(4, original.getTile(0, 0).getGridId());
    assertEquals(9, copy.getTiles().getFirst().getGridId());
    assertEquals(9, copy.getRawTileData().getTiles().getFirst().getGridId());
  }

  @Test
  void parsesIndependentEmptyTiles() throws InvalidTileLayerException {
    var tiles = TileData.parseCsvData("0,0");

    assertNotSame(tiles.get(0), tiles.get(1));
  }

  @Test
  void editedTilesArePersisted() throws Exception {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("tile-edit-save");
    map.setWidth(3);
    map.setHeight(2);
    map.setTileWidth(16);
    map.setTileHeight(16);
    TileLayer layer = new TileLayer(3, 2);
    layer.setName("ground");
    map.addLayer(layer);
    layer.setTile(2, 1, 17);
    Path target = Files.createTempFile("tile-edit-save", ".tmx");

    try {
      XmlUtilities.save(map, target);
      TmxMap restored = (TmxMap) Resources.maps().get(target.toUri().toURL());

      assertEquals(17, restored.getTileLayers().getFirst().getTile(2, 1).getGridId());
    } finally {
      Files.deleteIfExists(target);
    }
  }
}
