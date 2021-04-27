package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropTests {

  @Test
  void testGetStateIntact() {
    // arrange
    Prop prop = new Prop(0, 0, null);
    prop.setIndestructible(true);

    // act
    PropState propState = prop.getState();

    // assert
    assertEquals(PropState.INTACT, propState);
    assertFalse(prop.isDead());
  }

  @Test
  void testGetStateDamaged() {
    // arrange
    Prop prop = new Prop(0, 0, null);
    prop.setIndestructible(false);
    prop.getHitPoints().modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACT, 60));

    // act
    PropState propState = prop.getState();

    // assert
    assertEquals(PropState.DAMAGED, propState);
    assertFalse(prop.isDead());
  }

  @Test
  void testGetStateDestroyed() {
    // arrange
    Prop prop = new Prop(0, 0, null);
    prop.setIndestructible(false);
    prop.getHitPoints().modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACT, 150));

    // act
    PropState propState = prop.getState();

    // assert
    assertEquals(PropState.DESTROYED, propState);
    assertTrue(prop.isDead());
  }
}
