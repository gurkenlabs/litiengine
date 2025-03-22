package de.gurkenlabs.litiengine.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PropertyModifierTest {

  @Test
  void testInitAttributeModifier() {
    final PropertyModifier<Integer> testPropertyModifier =
      new PropertyModifier<>(Modification.ADD, 5);
    assertEquals(Modification.ADD, testPropertyModifier.getModification());
    assertEquals(5.0, testPropertyModifier.getModifyValue());
    assertTrue(testPropertyModifier.isActive());
  }

  @Test
  void testModifyValueWithInactiveAttributeModifier() {
    final PropertyModifier<Integer> testPropertyModifierActive =
      new PropertyModifier<>(Modification.ADD, 5);
    final PropertyModifier<Integer> testPropertyModifierInactive =
      new PropertyModifier<>(Modification.ADD, 5);
    testPropertyModifierActive.setActive(true);
    testPropertyModifierInactive.setActive(false);

    assertEquals(7, testPropertyModifierActive.modify(2));
    assertEquals(2, testPropertyModifierInactive.modify(2));
  }

  @Test
  void testAttributeModifierListener(){
    final PropertyModifier<Integer> propertyModifier =
      new PropertyModifier<>(Modification.ADD, 5);

    final int[] eventFired = {0};
    propertyModifier.addListener(_ -> eventFired[0]++);

    propertyModifier.setModifyValue(123);
    assertEquals(1, eventFired[0]);

    propertyModifier.setActive(false);

    assertEquals(2, eventFired[0]);
  }
}
