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
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class CreaturePanelTest {

  @Test
  void getCreatureSpriteNameExtractsBaseFromStateToken() {
    assertEquals("goblin", CreaturePanel.getCreatureSpriteName("goblin-idle-down"));
    assertEquals("goblin", CreaturePanel.getCreatureSpriteName("goblin-move-left"));
    assertEquals("goblin", CreaturePanel.getCreatureSpriteName("goblin-walk-right"));
    assertEquals("goblin", CreaturePanel.getCreatureSpriteName("goblin-dead"));
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
}
