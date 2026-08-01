package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MapPropertyPanelTest {

  @AfterEach
  void cleanup() throws Exception {
    Method terminate = Game.class.getDeclaredMethod("terminate");
    terminate.setAccessible(true);
    terminate.invoke(null);
  }

  @Test
  void saveChangesAppliesShadowColorToLiveEnvironment() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("shadow-color-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    Game.world().loadEnvironment(map);

    MapPropertyPanel panel = new MapPropertyPanel();
    panel.bind(map);
    shadowColorComponent(panel).setColor(Color.BLUE);

    assertEquals(Color.BLUE, Game.world().environment().getStaticShadowLayer().getColor());
  }

  @Test
  void addingTilesetPublishesImmediateChange() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("tileset-change-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    Game.world().loadEnvironment(map);
    MapPropertyPanel panel = new MapPropertyPanel();
    panel.bind(map);
    AtomicReference<de.gurkenlabs.litiengine.environment.tilemap.IMap> changed = new AtomicReference<>();
    panel.onTilesetsChanged(changed::set);
    Tileset tileset = new Tileset();
    tileset.setName("terrain");

    panel.addTileset(tileset);

    assertEquals(map, changed.get());
    assertEquals(tileset, map.getTilesets().getFirst());
  }

  @Test
  void bindingNullClearsMapAndNestedTilesetControls() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("old-map");
    Tileset tileset = new Tileset();
    tileset.setName("old-tileset");
    map.getTilesets().add(tileset);
    MapPropertyPanel panel = new MapPropertyPanel();
    panel.bind(map);

    panel.bind(null);

    assertEquals("", textField(panel, "textFieldName").getText());
    assertEquals("", textField(panel, "textFieldTitle").getText());
    assertEquals("", textArea(panel, "textFieldDesc").getText());
    assertEquals(0, tilesetPanel(panel).getTabCountForTest());
    assertNull(field(panel, "dataSource"));
  }

  private static ColorComponent shadowColorComponent(MapPropertyPanel panel) throws Exception {
    return (ColorComponent) field(panel, "shadowColorComponent");
  }

  private static javax.swing.JTextField textField(MapPropertyPanel panel, String name) throws Exception {
    return (javax.swing.JTextField) field(panel, name);
  }

  private static javax.swing.JTextArea textArea(MapPropertyPanel panel, String name) throws Exception {
    return (javax.swing.JTextArea) field(panel, name);
  }

  private static TilesetTabsPanel tilesetPanel(MapPropertyPanel panel) throws Exception {
    return (TilesetTabsPanel) field(panel, "tilesetPanel");
  }

  private static Object field(MapPropertyPanel panel, String name) throws Exception {
    Field field = MapPropertyPanel.class.getDeclaredField(name);
    field.setAccessible(true);
    return field.get(panel);
  }
}
