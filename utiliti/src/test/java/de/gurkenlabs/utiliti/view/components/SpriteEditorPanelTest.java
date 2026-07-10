package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SpriteEditorPanelTest {

  @AfterEach
  void cleanup() throws Exception {
    UndoManager.clearAll();
    var terminate = Game.class.getDeclaredMethod("terminate");
    terminate.setAccessible(true);
    terminate.invoke(null);
  }

  @Test
  void spriteInspectorEditsAreUndoable() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("sprite-inspector-undo-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    Game.world().loadEnvironment(map);
    SpritesheetResource sprite = new SpritesheetResource(new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB), "before", 2, 2);
    Editor.instance().getGameFile().getSpriteSheets().add(sprite);
    SpriteEditorPanel panel = new SpriteEditorPanel();
    panel.bind(sprite);

    panel.setNameForTest("after");

    assertEquals("after", sprite.getName());
    UndoManager.instance().undo();
    assertEquals("before", sprite.getName());
    UndoManager.instance().redo();
    assertEquals("after", sprite.getName());
  }

  @Test
  void gridDimensionsAreUndoable() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("sprite-grid-undo-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    Game.world().loadEnvironment(map);
    SpritesheetResource sprite = new SpritesheetResource(new BufferedImage(8, 4, BufferedImage.TYPE_INT_ARGB), "grid", 4, 2);
    Editor.instance().getGameFile().getSpriteSheets().add(sprite);
    SpriteEditorPanel panel = new SpriteEditorPanel();
    panel.bind(sprite);

    panel.setColumnsForTest(4);

    assertEquals(2, sprite.getWidth());
    UndoManager.instance().undo();
    assertEquals(4, sprite.getWidth());
    assertEquals(2, sprite.getHeight());
  }
}
