package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PropTests {

    @Test
    public void testGetStateIntact(){
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
    public void testGetStateDamaged(){
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
    public void testGetStateDestroyed(){
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
