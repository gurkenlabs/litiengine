package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.entities.CombatEntity;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import java.awt.image.BufferedImage;
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
}
