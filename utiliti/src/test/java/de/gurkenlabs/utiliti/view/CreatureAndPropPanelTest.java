package de.gurkenlabs.utiliti.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.utiliti.view.components.CreaturePanel;
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
    assertEquals(PropState.INTACT, getPropState(mapObject));

    // Current HP = 50 -> DAMAGED (<= 50% max HP)
    mapObject.setValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, 50);
    assertEquals(PropState.DAMAGED, getPropState(mapObject));

    // Current HP = 0 -> DESTROYED
    mapObject.setValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, 0);
    assertEquals(PropState.DESTROYED, getPropState(mapObject));

    // Indestructible prop is always INTACT regardless of current HP
    mapObject.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, true);
    assertEquals(PropState.INTACT, getPropState(mapObject));
  }

  private static PropState getPropState(IMapObject mapObject) {
    int maxHp = mapObject.getIntValue(MapObjectProperty.COMBAT_HITPOINTS, 100);
    int currentHp = mapObject.hasCustomProperty(MapObjectProperty.COMBAT_CURRENT_HITPOINTS)
        ? mapObject.getIntValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS)
        : maxHp;
    boolean indestructible = mapObject.getBoolValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, false);

    if (!indestructible && currentHp <= 0) {
      return PropState.DESTROYED;
    } else if (!indestructible && currentHp <= maxHp * 0.5) {
      return PropState.DAMAGED;
    }
    return PropState.INTACT;
  }
}
