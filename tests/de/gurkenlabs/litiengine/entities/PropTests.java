package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropTests {

    @Test
    public void testPropStateIntact(){
        Prop prop = new Prop(0, 0, null);
        prop.setIndestructible(true);

        PropState propState = prop.getState();

        assertEquals(PropState.INTACT, propState);
        assertEquals(false, prop.isDead());
    }

    @Test
    public void testPropStateDamaged(){
        Prop prop = new Prop(0, 0, null);
        prop.setIndestructible(false);
        prop.getHitPoints().modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACT, 60));

        PropState propState = prop.getState();

        assertEquals(PropState.DAMAGED, propState);
        assertEquals(false, prop.isDead());
    }

    @Test
    public void testPropStateDestroyed(){
        Prop prop = new Prop(0, 0, null);
        prop.setIndestructible(false);
        prop.getHitPoints().modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACT, 150));

        PropState propState = prop.getState();

        assertEquals(PropState.DESTROYED, propState);
        assertEquals(true, prop.isDead());

    }
}
