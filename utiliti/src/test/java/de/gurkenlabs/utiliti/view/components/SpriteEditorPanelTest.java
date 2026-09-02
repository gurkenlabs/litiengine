package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

  @Test
  void keyframeDurationsAreUndoable() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("sprite-duration-undo-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    Game.world().loadEnvironment(map);
    SpritesheetResource sprite = new SpritesheetResource(
        new BufferedImage(6, 2, BufferedImage.TYPE_INT_ARGB), "duration", 2, 2);
    sprite.setKeyframes(new int[] {100, 120, 140});
    Editor.instance().getGameFile().getSpriteSheets().add(sprite);
    SpriteEditorPanel panel = new SpriteEditorPanel();
    panel.bind(sprite);

    panel.setDurationForTest(1, 250);

    assertEquals(250, sprite.getKeyframes()[1]);
    UndoManager.instance().undo();
    assertEquals(120, sprite.getKeyframes()[1]);
  }

  @Test
  void durationFooterWrapsSummaryWithoutOverlappingApplyButton() {
    SpriteEditorPanel panel = new SpriteEditorPanel();
    JButton apply = findButton(panel, Resources.strings().get("assetpanel_animation_apply"));
    JPanel footer = (JPanel) apply.getParent();
    JLabel summary = (JLabel) footer.getComponent(footer.getComponentCount() - 1);
    summary.setText(Resources.strings().get("spriteEditor_fpsEquivalent", "8.33"));
    footer.setSize(320, 1);
    int preferredHeight = footer.getPreferredSize().height;
    footer.setSize(320, preferredHeight);

    footer.doLayout();

    assertFalse(apply.getBounds().intersects(summary.getBounds()));
    assertEquals(preferredHeight, footer.getMaximumSize().height);
    for (Component component : footer.getComponents()) {
      assertTrue(component.getY() + component.getHeight() <= footer.getHeight());
    }
    panel.removeNotify();
  }

  @Test
  void getFamilyVariantsIncludesMirroredCounterpartsWithIndicator() {
    Editor.instance().getGameFile().getSpriteSheets().clear();
    SpritesheetResource idleLeft = new SpritesheetResource(
        new BufferedImage(4, 2, BufferedImage.TYPE_INT_ARGB), "warrior-idle-left", 2, 2);
    SpritesheetResource walkLeft = new SpritesheetResource(
        new BufferedImage(4, 2, BufferedImage.TYPE_INT_ARGB), "warrior-walk-left", 2, 2);
    Editor.instance().getGameFile().getSpriteSheets().add(idleLeft);
    Editor.instance().getGameFile().getSpriteSheets().add(walkLeft);

    List<SpriteEditorPanel.VariantItem> variants = SpriteEditorPanel.getFamilyVariants("warrior-idle-left");

    assertEquals(4, variants.size());
    assertEquals("warrior-idle-left", variants.get(0).name());
    assertFalse(variants.get(0).isMirrored());

    assertEquals("warrior-idle-right", variants.get(1).name());
    assertTrue(variants.get(1).isMirrored());
    assertTrue(variants.get(1).toString().contains("mirrored"));

    assertEquals("warrior-walk-left", variants.get(2).name());
    assertFalse(variants.get(2).isMirrored());

    assertEquals("warrior-walk-right", variants.get(3).name());
    assertTrue(variants.get(3).isMirrored());
    assertTrue(variants.get(3).toString().contains("mirrored"));

    Editor.instance().getGameFile().getSpriteSheets().clear();
  }

  @Test
  void editingMirroredVariantMaterializesResourceAndIsUndoable() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName("mirrored-materialize-test");
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    Game.world().loadEnvironment(map);

    Editor.instance().getGameFile().getSpriteSheets().clear();
    SpritesheetResource idleLeft = new SpritesheetResource(
        new BufferedImage(4, 2, BufferedImage.TYPE_INT_ARGB), "jorge-idle-left", 2, 2);
    idleLeft.setKeyframes(new int[] {100, 100});
    Editor.instance().getGameFile().getSpriteSheets().add(idleLeft);

    assertTrue(SpriteEditorPanel.isVirtualMirrored("jorge-idle-right"));

    SpritesheetResource virtualRight = SpriteEditorPanel.createMirroredResource(idleLeft, "jorge-idle-right");
    SpriteEditorPanel panel = new SpriteEditorPanel();
    panel.bind(virtualRight);

    // Edit keyframe on mirrored variant
    panel.setDurationForTest(1, 240);

    // Should now be materialized into GameFile
    SpritesheetResource materialized = SpriteEditorPanel.findSpriteResource("jorge-idle-right");
    assertEquals(virtualRight, materialized);
    assertEquals(240, materialized.getKeyframes()[1]);
    assertEquals(100, materialized.getKeyframes()[0]);
    assertFalse(SpriteEditorPanel.isVirtualMirrored("jorge-idle-right"));

    // Original left remains unchanged
    assertEquals(100, idleLeft.getKeyframes()[1]);

    // Undo should remove the materialized resource
    UndoManager.instance().undo();
    assertEquals(null, SpriteEditorPanel.findSpriteResource("jorge-idle-right"));
    assertTrue(SpriteEditorPanel.isVirtualMirrored("jorge-idle-right"));

    Editor.instance().getGameFile().getSpriteSheets().clear();
  }

  private static JButton findButton(Container root, String text) {
    for (Component component : root.getComponents()) {
      if (component instanceof JButton button && text.equals(button.getText())) {
        return button;
      }
      if (component instanceof Container container) {
        JButton found = findButtonOrNull(container, text);
        if (found != null) {
          return found;
        }
      }
    }
    throw new AssertionError("Button not found: " + text);
  }

  private static JButton findButtonOrNull(Container root, String text) {
    for (Component component : root.getComponents()) {
      if (component instanceof JButton button && text.equals(button.getText())) {
        return button;
      }
      if (component instanceof Container container) {
        JButton found = findButtonOrNull(container, text);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
