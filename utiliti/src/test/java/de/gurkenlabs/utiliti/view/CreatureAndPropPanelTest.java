package de.gurkenlabs.utiliti.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.utiliti.view.components.CreaturePanel;
import de.gurkenlabs.utiliti.view.components.PropPanel;
import org.junit.jupiter.api.Test;

class CreatureAndPropPanelTest {

  @Test
  void testCreatureStartDeadApplication() {
    IMapObject mapObject = new MapObject();
    mapObject.setValue(MapObjectProperty.COMBAT_HITPOINTS, 100);

    assertFalse(CreaturePanel.isStartDead(mapObject));

    // Apply start dead = true
    CreaturePanel.applyStartDead(mapObject, true);
    assertTrue(CreaturePanel.isStartDead(mapObject));
    assertEquals(0, mapObject.getIntValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS));
    assertFalse(mapObject.getBoolValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, false));

    // Apply start dead = false
    CreaturePanel.applyStartDead(mapObject, false);
    assertFalse(CreaturePanel.isStartDead(mapObject));
    assertFalse(mapObject.hasCustomProperty(MapObjectProperty.COMBAT_CURRENT_HITPOINTS));
  }

  @Test
  void testPropStateResolutionFromHitpoints() {
    IMapObject mapObject = new MapObject();
    mapObject.setValue(MapObjectProperty.COMBAT_HITPOINTS, 100);

    // Default (no current HP override) -> INTACT
    assertEquals(PropState.INTACT, PropPanel.resolvePropState(mapObject));

    // Current HP = 50 -> DAMAGED (<= 50% max HP)
    mapObject.setValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, 50);
    assertEquals(PropState.DAMAGED, PropPanel.resolvePropState(mapObject));

    // Current HP = 0 -> DESTROYED
    mapObject.setValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, 0);
    assertEquals(PropState.DESTROYED, PropPanel.resolvePropState(mapObject));

    // Indestructible prop is always INTACT regardless of current HP
    mapObject.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, true);
    assertEquals(PropState.INTACT, PropPanel.resolvePropState(mapObject));
  }

  @Test
  void testCreatureStartDeadSyncWithCurrentHitpoints() {
    IMapObject mapObject = new MapObject();
    mapObject.setValue(MapObjectProperty.COMBAT_HITPOINTS, 100);

    // Setting COMBAT_CURRENT_HITPOINTS to 0 makes isStartDead return true
    mapObject.setValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, 0);
    assertTrue(CreaturePanel.isStartDead(mapObject));

    // Setting COMBAT_CURRENT_HITPOINTS to > 0 makes isStartDead return false
    mapObject.setValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, 50);
    assertFalse(CreaturePanel.isStartDead(mapObject));

    // Making indestructible makes isStartDead return false even if current HP is 0
    mapObject.setValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, 0);
    mapObject.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, true);
    assertFalse(CreaturePanel.isStartDead(mapObject));
  }
}
