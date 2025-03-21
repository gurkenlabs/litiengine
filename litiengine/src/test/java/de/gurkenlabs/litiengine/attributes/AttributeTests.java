package de.gurkenlabs.litiengine.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * The Class AttributeTests.
 */
class AttributeTests {

  @Test
  void testAttributeInitialization() {
    final Attribute<Integer> testAttribute = new Attribute<>(10);
    assertEquals(10, testAttribute.get().intValue());
  }

  @Test
  void testModifyBaseValueWithAddModificationByte() {
    // arrange
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);

    // act
    testAttributeByte.modifyBaseValue(new AttributeModifier<>(Modification.ADD, (byte) 100));

    // assert
    assertEquals((byte) 110, testAttributeByte.get().byteValue());
  }

  @Test
  void testModifyBaseValueWithAddModificationShort() {
    // arrange
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);

    // act
    testAttributeShort.modifyBaseValue(new AttributeModifier<>(Modification.ADD, (short) 100));

    // assert
    assertEquals((short) 110, testAttributeShort.get().shortValue());
  }

  @Test
  void testModifyBaseValueWithAddModificationInteger() {
    // arrange
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);

    // act
    testAttributeInt.modifyBaseValue(new AttributeModifier<>(Modification.ADD, 100));

    // assert
    assertEquals(110, testAttributeInt.get().intValue());
  }

  @Test
  void testModifyBaseValueWithAddModificationLong() {
    // arrange
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    // act
    testAttributeLong.modifyBaseValue(new AttributeModifier<>(Modification.ADD, 1000L));

    // assert
    assertEquals(1010L, testAttributeLong.get().longValue());
  }

  @Test
  void testModifyBaseValueWithAddModificationFloat() {
    // arrange
    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);

    // act
    testAttributeFloat.modifyBaseValue(new AttributeModifier<>(Modification.ADD, 101.1f));

    // assert
    assertEquals(111.1f, testAttributeFloat.get(), 0.0001f);
  }

  @Test
  void testModifyBaseValueWithAddModificationDouble() {
    // arrange
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    // act
    testAttributeDouble.modifyBaseValue(new AttributeModifier<>(Modification.ADD, 101.1));

    // assert
    assertEquals(111.1, testAttributeDouble.get(), 0.0000001);
  }


  @Test
  void testModifyBaseValueWithDivideModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modifyBaseValue(new AttributeModifier<>(Modification.DIVIDE, 2));
    testAttributeShort.modifyBaseValue(new AttributeModifier<>(Modification.DIVIDE, 2));
    testAttributeInt.modifyBaseValue(new AttributeModifier<>(Modification.DIVIDE, 2));
    testAttributeLong.modifyBaseValue(new AttributeModifier<>(Modification.DIVIDE, 2));

    testAttributeFloat.modifyBaseValue(new AttributeModifier<>(Modification.DIVIDE, 3));
    testAttributeDouble.modifyBaseValue(new AttributeModifier<>(Modification.DIVIDE, 3));

    assertEquals((byte) 5, testAttributeByte.get().byteValue());
    assertEquals((short) 5, testAttributeShort.get().shortValue());
    assertEquals(5, testAttributeInt.get().intValue());
    assertEquals(5L, testAttributeLong.get().longValue());

    assertEquals(3.333333333333f, testAttributeFloat.get(), 0.0001f);
    assertEquals(3.333333333333, testAttributeDouble.get(), 0.0000001);
  }

  @Test
  void testModifyBaseValueWithMultiplyModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modifyBaseValue(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testAttributeShort.modifyBaseValue(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testAttributeInt.modifyBaseValue(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testAttributeLong.modifyBaseValue(new AttributeModifier<>(Modification.MULTIPLY, 2));

    testAttributeFloat.modifyBaseValue(new AttributeModifier<>(Modification.MULTIPLY, 2.1));
    testAttributeDouble.modifyBaseValue(new AttributeModifier<>(Modification.MULTIPLY, 2.1));

    assertEquals((byte) 20, testAttributeByte.get().byteValue());
    assertEquals((short) 20, testAttributeShort.get().shortValue());
    assertEquals(20, testAttributeInt.get().intValue());
    assertEquals(20L, testAttributeLong.get().longValue());

    assertEquals(21f, testAttributeFloat.get(), 0.0001f);
    assertEquals(21, testAttributeDouble.get(), 0.0000001);
  }

  @Test
  void testModifyBaseValueWithSetModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modifyBaseValue(new AttributeModifier<>(Modification.SET, 20));
    testAttributeShort.modifyBaseValue(new AttributeModifier<>(Modification.SET, 20));
    testAttributeInt.modifyBaseValue(new AttributeModifier<>(Modification.SET, 20));
    testAttributeLong.modifyBaseValue(new AttributeModifier<>(Modification.SET, 20));

    testAttributeFloat.modifyBaseValue(new AttributeModifier<>(Modification.SET, 21));
    testAttributeDouble.modifyBaseValue(new AttributeModifier<>(Modification.SET, 21));

    assertEquals((byte) 20, testAttributeByte.get().byteValue());
    assertEquals((short) 20, testAttributeShort.get().shortValue());
    assertEquals(20, testAttributeInt.get().intValue());
    assertEquals(20L, testAttributeLong.get().longValue());

    assertEquals(21f, testAttributeFloat.get(), 0.0001f);
    assertEquals(21, testAttributeDouble.get(), 0.0000001);
  }

  @Test
  void testModifyBaseValueWithSubstractModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACT, (byte) 1));
    testAttributeShort.modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACT, (short) 1));
    testAttributeInt.modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACT, 1));
    testAttributeLong.modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACT, 1));

    testAttributeFloat.modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACT, 0.9f));
    testAttributeDouble.modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACT, 0.9));

    assertEquals((byte) 9, testAttributeByte.get().byteValue());
    assertEquals((short) 9, testAttributeShort.get().shortValue());
    assertEquals(9, testAttributeInt.get().intValue());
    assertEquals(9L, testAttributeLong.get().longValue());

    assertEquals(9.1f, testAttributeFloat.get(), 0.0001f);
    assertEquals(9.1, testAttributeDouble.get(), 0.0000001);
  }

  @Test
  void testAddModifierWithNewModifier() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final AttributeModifier<Byte> multiplyAttributeModifier =
      new AttributeModifier<>(Modification.MULTIPLY, 2);
    final AttributeModifier<Byte> addAttributeModifier =
      new AttributeModifier<>(Modification.ADD, 50);

    assertEquals(Collections.emptyList(), testAttributeByte.getModifiers());

    testAttributeByte.addModifier(multiplyAttributeModifier);
    assertEquals(
      Collections.singletonList(multiplyAttributeModifier), testAttributeByte.getModifiers());

    testAttributeByte.addModifier(addAttributeModifier);
    assertEquals(
      Arrays.asList(addAttributeModifier, multiplyAttributeModifier),
      testAttributeByte.getModifiers());
  }

  @Test
  void testAddModifierWithExistingModifier() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final AttributeModifier<Byte> addAttributeModifier =
      new AttributeModifier<>(Modification.ADD, 50);

    assertEquals(Collections.emptyList(), testAttributeByte.getModifiers());

    testAttributeByte.addModifier(addAttributeModifier);
    assertEquals(Collections.singletonList(addAttributeModifier), testAttributeByte.getModifiers());

    testAttributeByte.addModifier(addAttributeModifier);
    assertEquals(Collections.singletonList(addAttributeModifier), testAttributeByte.getModifiers());
  }

  @Test
  void testRemoveModifier() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final AttributeModifier<Byte> multiplyAttributeModifier =
      new AttributeModifier<>(Modification.MULTIPLY, 2);
    final AttributeModifier<Byte> addAttributeModifier =
      new AttributeModifier<>(Modification.ADD, 50);
    final AttributeModifier<Byte> subtractAttributeModifier =
      new AttributeModifier<>(Modification.SUBTRACT, 25);

    testAttributeByte.addModifier(multiplyAttributeModifier);
    testAttributeByte.addModifier(addAttributeModifier);
    testAttributeByte.addModifier(subtractAttributeModifier);
    assertEquals(
      Arrays.asList(addAttributeModifier, subtractAttributeModifier, multiplyAttributeModifier),
      testAttributeByte.getModifiers());

    testAttributeByte.removeModifier(subtractAttributeModifier);
    assertEquals(
      Arrays.asList(addAttributeModifier, multiplyAttributeModifier),
      testAttributeByte.getModifiers());
  }

  @Test
  void testIsModifierApplied() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final AttributeModifier<Byte> multiplyAttributeModifier =
      new AttributeModifier<>(Modification.MULTIPLY, 2);
    final AttributeModifier<Byte> addAttributeModifier =
      new AttributeModifier<>(Modification.ADD, 50);
    final AttributeModifier<Byte> subtractAttributeModifier =
      new AttributeModifier<>(Modification.SUBTRACT, 25);

    testAttributeByte.addModifier(multiplyAttributeModifier);
    testAttributeByte.addModifier(addAttributeModifier);
    assertEquals(
      Arrays.asList(addAttributeModifier, multiplyAttributeModifier),
      testAttributeByte.getModifiers());

    assertTrue(testAttributeByte.isModifierApplied(addAttributeModifier));
    assertTrue(testAttributeByte.isModifierApplied(multiplyAttributeModifier));
    assertFalse(testAttributeByte.isModifierApplied(subtractAttributeModifier));
  }

  @Test
  void testGetByte() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    testAttributeByte.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    assertEquals((byte) 20, testAttributeByte.get().byteValue());
  }

  @Test
  void testGetShort() {
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    testAttributeShort.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    assertEquals((short) 20, testAttributeShort.get().byteValue());
  }

  @Test
  void testGetInteger() {
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    testAttributeInt.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    assertEquals(20, testAttributeInt.get().intValue());
  }

  @Test
  void testGetLong() {
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);
    testAttributeLong.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    assertEquals(20L, testAttributeLong.get().longValue());
  }

  @Test
  void testGetFloat() {
    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    testAttributeFloat.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    assertEquals(20.0f, testAttributeFloat.get().floatValue());
  }

  @Test
  void testGetDouble() {
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);
    testAttributeDouble.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    assertEquals(20.0d, testAttributeDouble.get().doubleValue());
  }

  @Test
  void testToString() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testAttributeShort.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testAttributeInt.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testAttributeLong.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testAttributeFloat.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testAttributeDouble.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));

    assertEquals("20", testAttributeByte.toString());
    assertEquals("20", testAttributeShort.toString());
    assertEquals("20", testAttributeInt.toString());
    assertEquals("20", testAttributeLong.toString());
    assertEquals("20.0", testAttributeFloat.toString());
    assertEquals("20.0", testAttributeDouble.toString());
  }

  @Test
  void testValueChangedEvent(){
    final Attribute<Integer> testAttribute= new Attribute<>(10);

    final int[] eventFired = {0};
    testAttribute.onValueChanged(() -> eventFired[0]++);

    testAttribute.setBaseValue(11);
    assertEquals(1, eventFired[0]);

    testAttribute.modifyBaseValue(Modification.SET, 12);

    assertEquals(2, eventFired[0]);

    final AttributeModifier<Integer> attributeModifier =
      new AttributeModifier<>(Modification.ADD, 5);

    testAttribute.addModifier(attributeModifier);
    assertEquals(3, eventFired[0]);

    attributeModifier.setModifyValue(6);

    assertEquals(4, eventFired[0]);

    testAttribute.removeModifier(attributeModifier);

    assertEquals(5, eventFired[0]);  }
}
