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

    @Test
    public void testToStringNull(){
        Prop prop = new Prop(0, 0, null);
        prop.setMapId(1);
        prop.getMapId();
        assertEquals("#1: Prop (null)", prop.toString());
    }

    @Test
    public void testToStringEmpty(){
        Prop prop = new Prop(0, 0, "");
        prop.setMapId(1);
        prop.getMapId();
        assertEquals("#1: Prop ()", prop.toString());
    }

    @Test
    public void testToStringNotNull(){
        Prop prop = new Prop(0, 0, "Test");
        prop.setMapId(1);
        prop.getMapId();
        prop.setName("Test");
        prop.getName();
        assertEquals("#1: Test (Test)", prop.toString());
    }

}
