package de.gurkenlabs.litiengine.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AttributeModifierTests {

  @Test
  void testInitAttributeModifier() {
    final AttributeModifier<Integer> testAttributeModifier =
      new AttributeModifier<>(Modification.ADD, 5);
    assertEquals(Modification.ADD, testAttributeModifier.getModification());
    assertEquals(5.0, testAttributeModifier.getModifyValue());
    assertTrue(testAttributeModifier.isActive());
  }

  @Test
  void testModifyValueWithInactiveAttributeModifier() {
    final AttributeModifier<Integer> testAttributeModifierActive =
      new AttributeModifier<>(Modification.ADD, 5);
    final AttributeModifier<Integer> testAttributeModifierInactive =
      new AttributeModifier<>(Modification.ADD, 5);
    testAttributeModifierActive.setActive(true);
    testAttributeModifierInactive.setActive(false);

    assertEquals(7, testAttributeModifierActive.modify(2));
    assertEquals(2, testAttributeModifierInactive.modify(2));
  }

  @Test
  void testAttributeModifierListener(){
    final AttributeModifier<Integer> attributeModifier =
      new AttributeModifier<>(Modification.ADD, 5);

    final int[] eventFired = {0};
    attributeModifier.addListener(_ -> eventFired[0]++);

    attributeModifier.setModifyValue(123);
    assertEquals(1, eventFired[0]);

    attributeModifier.setActive(false);

    assertEquals(2, eventFired[0]);
  }
}
