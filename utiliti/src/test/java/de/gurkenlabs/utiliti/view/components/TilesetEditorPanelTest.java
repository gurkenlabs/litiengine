package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.environment.tilemap.xml.MapImage;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TilesetEntry;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.utiliti.controller.UndoManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

class TilesetEditorPanelTest {

  @AfterEach
  void cleanup() throws Exception {
    UndoManager.clearAll();
    var terminate = Game.class.getDeclaredMethod("terminate");
    terminate.setAccessible(true);
    terminate.invoke(null);
  }

  @Test
  void bindSelectsFirstTileAndShowsTileDetails() throws Exception {
    Tileset tileset = tileset("world", "tiles/world.png", 4, 2);
    TilesetEditorPanel panel = new TilesetEditorPanel();

    panel.bind(tileset);

    assertEquals(0, panel.getSelectedTileIdForTest());
    assertTrue(panel.getDetailTextForTest().contains("Tile 0"));
    assertTrue(panel.getDetailTextForTest().contains("gid 1"));
  }

  @Test
  void bindNullClearsSelection() {
    TilesetEditorPanel panel = new TilesetEditorPanel();

    panel.bind(null);

    assertEquals(-1, panel.getSelectedTileIdForTest());
  }

  @Test
  void typeControlUpdatesSelectedTileType() throws Exception {
    Tileset tileset = tileset("world", "tiles/world.png", 4, 2);
    TilesetEditorPanel panel = new TilesetEditorPanel();
    panel.bind(tileset);

    panel.setTypeTextForTest("wall");

    assertEquals("wall", tileset.getTile(0).getType());
    assertEquals("wall", panel.getTypeTextForTest());
  }

  @Test
  void customPropertyControlUpdatesSelectedTileProperties() throws Exception {
    Tileset tileset = tileset("world", "tiles/world.png", 4, 2);
    TilesetEditorPanel panel = new TilesetEditorPanel();
    panel.bind(tileset);

    panel.setCustomPropertyForTest("material", "stone");

    assertEquals("stone", tileset.getTile(0).getStringValue("material", null));
  }

  @Test
  void probabilityControlUpdatesSelectedTileProbability() throws Exception {
    Tileset tileset = tileset("world", "tiles/world.png", 4, 2);
    TilesetEditorPanel panel = new TilesetEditorPanel();
    panel.bind(tileset);

    panel.setProbabilityTextForTest("0.25");

    assertEquals(0.25, ((TilesetEntry) tileset.getTile(0)).getProbability(), 0.0001);
    assertEquals("0.25", panel.getProbabilityTextForTest());
  }

  @Test
  void animationFrameControlUpdatesSelectedTileAnimation() throws Exception {
    Tileset tileset = tileset("world", "tiles/world.png", 4, 2);
    TilesetEditorPanel panel = new TilesetEditorPanel();
    panel.bind(tileset);

    panel.addAnimationFrameForTest(1, 100);
    panel.addAnimationFrameForTest(2, 150);

    assertEquals(2, tileset.getTile(0).getAnimation().getFrames().size());
    assertEquals(250, tileset.getTile(0).getAnimation().getTotalDuration());
  }

  @Test
  void animationTilesAndSelectedFramesAreClassifiedForHighlighting() throws Exception {
    Tileset tileset = tileset("world", "tiles/world.png", 4, 2);
    TilesetEditorPanel panel = new TilesetEditorPanel();
    panel.bind(tileset);

    panel.addAnimationFrameForTest(1, 100);
    panel.addAnimationFrameForTest(2, 150);

    assertTrue(panel.isAnimationTileForTest(0));
    assertTrue(panel.isSelectedAnimationFrameForTest(1));
    assertTrue(panel.isSelectedAnimationFrameForTest(2));
  }

  @Test
  void tilesetNameControlUpdatesTilesetName() throws Exception {
    Tileset tileset = tileset("world", "tiles/world.png", 4, 2);
    TilesetEditorPanel panel = new TilesetEditorPanel();
    panel.bind(tileset);

    panel.setTilesetNameTextForTest("dungeon");

    assertEquals("dungeon", tileset.getName());
    assertEquals("dungeon", panel.getTilesetNameTextForTest());
  }

  @Test
  void tilesetCustomPropertyControlUpdatesTilesetProperties() throws Exception {
    Tileset tileset = tileset("world", "tiles/world.png", 4, 2);
    TilesetEditorPanel panel = new TilesetEditorPanel();
    panel.bind(tileset);

    panel.setTilesetCustomPropertyForTest("theme", "hospital");

    assertEquals("hospital", tileset.getStringValue("theme", null));
  }

  @Test
  void tilesetRenderSettingsUpdateTilesetMetadata() throws Exception {
    Tileset tileset = tileset("world", "tiles/world.png", 4, 2);
    TilesetEditorPanel panel = new TilesetEditorPanel();
    panel.bind(tileset);

    panel.setTilesetRenderSettingsForTest("center", "grid", "preserve-aspect-fit", "3", "-2");

    assertEquals("center", tileset.getObjectalignment());
    assertEquals("grid", tileset.getTilerendersize());
    assertEquals("preserve-aspect-fit", tileset.getFillmode());
    assertEquals(3, tileset.getTileOffset().getX());
    assertEquals(-2, tileset.getTileOffset().getY());
  }

  @Test
  void tileInspectorEditIsUndoable() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("tileset-inspector-undo-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    Game.world().loadEnvironment(map);
    Tileset tileset = tileset("world", "tiles/world.png", 4, 2);
    TilesetEditorPanel panel = new TilesetEditorPanel();
    panel.bind(tileset);

    panel.setTypeTextForTest("wall");
    UndoManager.instance().undo();

    assertEquals(null, tileset.getTile(0).getType());
    UndoManager.instance().redo();
    assertEquals("wall", tileset.getTile(0).getType());
  }

  private static Tileset tileset(String name, String imageSource, int tileCount, int columns) throws Exception {
    Tileset tileset = new Tileset();
    MapImage image = new MapImage();
    image.setSource(imageSource);
    set(tileset, "name", name);
    set(tileset, "image", image);
    set(tileset, "firstgid", 1);
    set(tileset, "tilewidth", 16);
    set(tileset, "tileheight", 16);
    set(tileset, "tilecount", tileCount);
    set(tileset, "columns", columns);
    set(tileset, "spacing", 0);
    set(tileset, "margin", 0);
    List<TilesetEntry> entries = new ArrayList<>();
    for (int i = 0; i < tileCount; i++) {
      entries.add(new TilesetEntry(tileset, i));
    }
    set(tileset, "allTiles", entries);
    return tileset;
  }

  private static void set(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
