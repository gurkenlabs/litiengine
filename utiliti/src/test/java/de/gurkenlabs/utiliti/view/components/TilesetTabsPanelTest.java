package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.environment.tilemap.TerrainType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangColor;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangSet;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import java.awt.Color;
import org.junit.jupiter.api.Test;

class TilesetTabsPanelTest {

  @Test
  void bindingSameMapKeepsSelectedTile() {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    Tileset tileset = new Tileset();
    tileset.setName("world");
    tileset.setTileWidth(16);
    tileset.setTileHeight(16);
    tileset.setTileCount(4);
    tileset.setColumns(2);
    map.getTilesets().add(tileset);
    TilesetTabsPanel panel = new TilesetTabsPanel();

    panel.bindIfMapChanged(map);
    TilesetEditorPanel editor = panel.getSelectedEditorForTest();
    editor.selectTileForTest(3);
    panel.bindIfMapChanged(map);

    assertSame(editor, panel.getSelectedEditorForTest());
    assertEquals(3, editor.getSelectedTileIdForTest());
  }

  @Test
  void selectedTabPublishesItsTerrain() {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    Tileset first = tileset("first", "grass");
    Tileset second = tileset("second", "snow");
    map.getTilesets().add(first);
    map.getTilesets().add(second);
    TilesetTabsPanel panel = new TilesetTabsPanel();

    panel.bind(map);

    assertEquals("grass", ToolManager.instance().getSelectedTerrain().getName());
    panel.select(second);
    assertEquals("snow", ToolManager.instance().getSelectedTerrain().getName());
  }

  @Test
  void sameMapTilesetChangesRefreshTabs() {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.getTilesets().add(tileset("first", "grass"));
    TilesetTabsPanel panel = new TilesetTabsPanel();
    panel.bindIfMapChanged(map);

    map.getTilesets().add(tileset("second", "snow"));
    panel.bindIfMapChanged(map);

    assertEquals(2, panel.getTabCountForTest());
  }

  @Test
  void bindingNullDisposesAllTilesetEditors() {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.getTilesets().add(tileset("first", "grass"));
    TilesetTabsPanel panel = new TilesetTabsPanel();
    panel.bind(map);

    panel.bind(null);

    assertEquals(0, panel.getTabCountForTest());
    assertNull(panel.getSelectedEditorForTest());
  }

  private static Tileset tileset(String name, String terrainName) {
    Tileset tileset = new Tileset();
    tileset.setName(name);
    WangSet set = new WangSet(name, TerrainType.CORNER);
    set.getTerrains().add(new WangColor(terrainName, Color.GREEN));
    tileset.getOrCreateTerrainSets().add(set);
    return tileset;
  }
}
