package de.gurkenlabs.litiengine.attributes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AttributeModifierTest {

    @Test
    void testInitAttributeModifier() {
        final AttributeModifier<Integer> testAttributeModifier = new AttributeModifier<>(Modification.ADD, 5);
        assertEquals(Modification.ADD, testAttributeModifier.getModification());
        assertEquals(5.0, testAttributeModifier.getModifyValue());
        assertTrue(testAttributeModifier.isActive());
    }

    @Test
    void testModifyValueWithInactiveAttributeModifier() {
        final AttributeModifier<Integer> testAttributeModifierActive = new AttributeModifier<>(Modification.ADD, 5);
        final AttributeModifier<Integer> testAttributeModifierInactive = new AttributeModifier<>(Modification.ADD, 5);
        testAttributeModifierActive.setActive(true);
        testAttributeModifierInactive.setActive(false);

        assertEquals(7, testAttributeModifierActive.modify(2));
        assertEquals(2, testAttributeModifierInactive.modify(2));
    }
}