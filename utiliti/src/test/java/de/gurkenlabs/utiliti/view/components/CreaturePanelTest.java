package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.entities.CombatEntity;
import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.utiliti.controller.Editor;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

class CreaturePanelTest {

  @Test
  void getCreatureSpriteNameExtractsBaseFromStateToken() {
    assertEquals("goblin", CreaturePanel.getCreatureSpriteName("goblin-idle-down"));
    assertEquals("goblin", CreaturePanel.getCreatureSpriteName("goblin-move-left"));
    assertEquals("goblin", CreaturePanel.getCreatureSpriteName("goblin-walk-right"));
    assertEquals("goblin", CreaturePanel.getCreatureSpriteName("goblin-dead"));
    assertEquals("undead-warrior", CreaturePanel.getCreatureSpriteName("undead-warrior-idle-left"));
  }

  @Test
  void getCreatureSpriteNameIgnoresStateTextInsideBaseName() {
    assertNull(CreaturePanel.getCreatureSpriteName("undead-warrior"));
    assertNull(CreaturePanel.getCreatureSpriteName("idlewood-tree"));
  }

  @Test
  void startDeadMapsToZeroHitpointsAndDestructible() {
    MapObject mapObject = new MapObject();
    mapObject.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, true);
    mapObject.setValue(MapObjectProperty.COMBAT_HITPOINTS, CombatEntity.DEFAULT_HITPOINTS);

    CreaturePanel.applyStartDead(mapObject, true);

    assertTrue(CreaturePanel.isStartDead(mapObject));
    assertFalse(mapObject.getBoolValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, true));
    assertEquals(CombatEntity.DEFAULT_HITPOINTS, mapObject.getIntValue(MapObjectProperty.COMBAT_HITPOINTS, -1));
    assertEquals(0, mapObject.getIntValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, -1));
  }

  @Test
  void disablingStartDeadRemovesCurrentHitpointsOverride() {
    MapObject mapObject = new MapObject();
    mapObject.setValue(MapObjectProperty.COMBAT_HITPOINTS, 75);
    mapObject.setValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, 0);

    CreaturePanel.applyStartDead(mapObject, false);

    assertFalse(CreaturePanel.isStartDead(mapObject));
    assertEquals(75, mapObject.getIntValue(MapObjectProperty.COMBAT_HITPOINTS, -1));
    assertFalse(mapObject.hasCustomProperty(MapObjectProperty.COMBAT_CURRENT_HITPOINTS));
  }

  @Test
  void bindPopulatesStartDeadFromCurrentHitpoints() {
    MapObject mapObject = new MapObject();
    mapObject.setValue(MapObjectProperty.COMBAT_HITPOINTS, CombatEntity.DEFAULT_HITPOINTS);
    mapObject.setValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, 0);
    CreaturePanel panel = new CreaturePanel();

    panel.setControlValues(mapObject);

    assertTrue(panel.isStartDeadSelectedForTest());
  }

  @Test
  void spritesheetClearRemovesCachedCreatureSpriteItems() {
    Resources.spritesheets().clear();
    new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "goblin-idle-down.png", 1, 1);
    CreaturePanel panel = new CreaturePanel();
    panel.bind(null);

    assertEquals(1, panel.getSpriteItemCountForTest());
    Resources.spritesheets().clear();
    assertEquals(0, panel.getSpriteItemCountForTest());
  }

  @Test
  void previewUsesSelectedDirectionAndLegacyWalkVariant() {
    Spritesheet left = new Spritesheet(
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "zombie12-walk-left.png", 1, 1);
    Spritesheet right = new Spritesheet(
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "zombie12-walk-right.png", 1, 1);

    String source = CreaturePanel.selectPreviewSpriteName(
        "Zombie12", Direction.LEFT, false, List.of(left, right));

    assertEquals("zombie12-walk-left", source);
    Resources.spritesheets().remove("zombie12-walk-left");
    Resources.spritesheets().remove("zombie12-walk-right");
  }

  @Test
  void animationPickerIncludesAllCreatureStatesIncludingDeath() {
    List<Spritesheet> sheets = List.of(
        new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "goblin-idle-down.png", 1, 1),
        new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "goblin-move-down.png", 1, 1),
        new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "goblin-dead.png", 1, 1));

    assertEquals(List.of("goblin-dead", "goblin-idle-down", "goblin-move-down"),
        List.copyOf(CreaturePanel.getAnimationSpriteNames("goblin", sheets).keySet()));

    Resources.spritesheets().remove("goblin-idle-down");
    Resources.spritesheets().remove("goblin-move-down");
    Resources.spritesheets().remove("goblin-dead");
  }

  @Test
  @ResourceLock("default-locale")
  void previewSpriteLookupIsLocaleIndependent() {
    Locale originalLocale = Locale.getDefault();
    try {
      Locale.setDefault(Locale.forLanguageTag("tr-TR"));
      Spritesheet right = new Spritesheet(
          new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "knight-walk-right.png", 1, 1);

      assertEquals("knight-walk-right", CreaturePanel.selectPreviewSpriteName(
          "KNIGHT", Direction.RIGHT, false, List.of(right)));
    } finally {
      Locale.setDefault(originalLocale);
    }
  }

  @Test
  void directionalCreaturePreviewOpensPersistedSpriteOnDoubleClick() {
    BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    SpritesheetResource resource = new SpritesheetResource(image, "zombie12-walk-left", 2, 2);
    Editor.instance().getGameFile().getSpriteSheets().add(resource);
    Resources.spritesheets().load(resource);
    AtomicReference<SpritesheetResource> opened = new AtomicReference<>();
    CreaturePanel panel = new CreaturePanel(opened::set);
    MapObject mapObject = new MapObject();
    mapObject.setValue(MapObjectProperty.SPRITESHEETNAME, "zombie12");
    mapObject.setValue(MapObjectProperty.SPAWN_DIRECTION, Direction.LEFT);

    panel.bind(mapObject);
    panel.doubleClickPreviewForTest();

    assertEquals(resource, opened.get());
    Resources.spritesheets().remove(resource.getName());
    Editor.instance().getGameFile().getSpriteSheets().remove(resource);
  }

  @Test
  void mirroredCreaturePreviewOpensMirroredResourceOnDoubleClick() {
    BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    SpritesheetResource resource = new SpritesheetResource(image, "zombie12-walk-right", 2, 2);
    Editor.instance().getGameFile().getSpriteSheets().add(resource);
    Resources.spritesheets().load(resource);
    new Spritesheet(image, "zombie12-walk-left.png", 2, 2);
    AtomicReference<SpritesheetResource> opened = new AtomicReference<>();
    CreaturePanel panel = new CreaturePanel(opened::set);
    MapObject mapObject = new MapObject();
    mapObject.setValue(MapObjectProperty.SPRITESHEETNAME, "zombie12");
    mapObject.setValue(MapObjectProperty.SPAWN_DIRECTION, Direction.LEFT);

    panel.bind(mapObject);
    panel.doubleClickPreviewForTest();

    assertEquals("zombie12-walk-left", opened.get().getName());
    assertTrue(SpriteEditorPanel.isVirtualMirrored("zombie12-walk-left"));
    Resources.spritesheets().remove("zombie12-walk-left");
    Resources.spritesheets().remove(resource.getName());
    Editor.instance().getGameFile().getSpriteSheets().remove(resource);
  }

  @Test
  void isMirroredIdentifiesDynamicFallbackCounterparts() {
    BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    SpritesheetResource resource = new SpritesheetResource(image, "goblin-idle-left", 2, 2);
    Editor.instance().getGameFile().getSpriteSheets().add(resource);

    assertTrue(CreaturePanel.isMirrored("goblin-idle-right"));
    assertFalse(CreaturePanel.isMirrored("goblin-idle-left"));
    assertFalse(CreaturePanel.isMirrored("goblin-idle-down"));

    Editor.instance().getGameFile().getSpriteSheets().remove(resource);
  }

  @Test
  void getDirectionFromAnimationNameExtractsCorrectDirection() {
    assertEquals(Direction.RIGHT, CreaturePanel.getDirectionFromAnimationName("walk-right"));
    assertEquals(Direction.LEFT, CreaturePanel.getDirectionFromAnimationName("zombie-idle-left"));
    assertEquals(Direction.DOWN, CreaturePanel.getDirectionFromAnimationName("attack-down"));
    assertEquals(Direction.UP, CreaturePanel.getDirectionFromAnimationName("dead-up"));
    assertEquals(null, CreaturePanel.getDirectionFromAnimationName("nondirectional"));
    assertEquals(null, CreaturePanel.getDirectionFromAnimationName(null));
  }

  @Test
  void selectPreviewSpriteNameSelectsMirroredCounterpartWhenAvailable() {
    BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, 0xffffffff);
    SpritesheetResource resource = new SpritesheetResource(image, "walter-walk-left", 2, 2);
    Editor.instance().getGameFile().getSpriteSheets().add(resource);
    Resources.spritesheets().load(resource);

    String selected = CreaturePanel.selectPreviewSpriteName("walter", Direction.RIGHT, false, Resources.spritesheets().getAll());
    assertEquals("walter-walk-right", selected);

    Spritesheet loadedMirrored = CreaturePanel.getOrLoadSpritesheet("walter-walk-right");
    assertEquals("walter-walk-right", loadedMirrored.getName());
    assertEquals(2, loadedMirrored.getSpriteWidth());

    Resources.spritesheets().remove("walter-walk-left");
    Resources.spritesheets().remove("walter-walk-right");
    Editor.instance().getGameFile().getSpriteSheets().remove(resource);
  }

  @Test
  void isDeathAnimationDetectsDeathVariants() {
    assertTrue(CreaturePanel.isDeathAnimation("dead"));
    assertTrue(CreaturePanel.isDeathAnimation("dead-left"));
    assertTrue(CreaturePanel.isDeathAnimation("zombie-walter-dead"));
    assertTrue(CreaturePanel.isDeathAnimation("zombie-walter-dead-right"));
    assertFalse(CreaturePanel.isDeathAnimation("walk-left"));
    assertFalse(CreaturePanel.isDeathAnimation("idle"));
    assertFalse(CreaturePanel.isDeathAnimation(null));
  }

  @Test
  void selectingDeathAnimationSyncsStartDeadCheckbox() {
    BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    SpritesheetResource liveRes = new SpritesheetResource(image, "syncdead-walk-left", 2, 2);
    SpritesheetResource deadRes = new SpritesheetResource(image, "syncdead-dead", 2, 2);
    Editor.instance().getGameFile().getSpriteSheets().add(liveRes);
    Editor.instance().getGameFile().getSpriteSheets().add(deadRes);
    Resources.spritesheets().load(liveRes);
    Resources.spritesheets().load(deadRes);

    CreaturePanel panel = new CreaturePanel();
    MapObject mapObject = new MapObject();
    mapObject.setValue(MapObjectProperty.SPRITESHEETNAME, "syncdead");
    mapObject.setValue(MapObjectProperty.SPAWN_DIRECTION, Direction.LEFT);

    panel.bind(mapObject);
    assertFalse(panel.isStartDeadSelectedForTest());

    // Select the "dead" item in the animation combo
    javax.swing.JComboBox<javax.swing.JLabel> combo = panel.getComboBoxAnimationsForTest();
    for (int i = 0; i < combo.getItemCount(); i++) {
      javax.swing.JLabel lbl = combo.getItemAt(i);
      if (lbl != null && "dead".equalsIgnoreCase(lbl.getText())) {
        combo.setSelectedItem(lbl);
        break;
      }
    }

    assertTrue(panel.isStartDeadSelectedForTest());
    assertEquals(0, mapObject.getIntValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS));

    // Select the "walk-left" item
    for (int i = 0; i < combo.getItemCount(); i++) {
      javax.swing.JLabel lbl = combo.getItemAt(i);
      if (lbl != null && "walk-left".equalsIgnoreCase(lbl.getText())) {
        combo.setSelectedItem(lbl);
        break;
      }
    }

    assertFalse(panel.isStartDeadSelectedForTest());

    Resources.spritesheets().remove("syncdead-walk-left");
    Resources.spritesheets().remove("syncdead-dead");
    Editor.instance().getGameFile().getSpriteSheets().remove(liveRes);
    Editor.instance().getGameFile().getSpriteSheets().remove(deadRes);
  }
}
