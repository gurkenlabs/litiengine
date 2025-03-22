package de.gurkenlabs.litiengine.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * The Class AttributeTests.
 */
class AttributeTests {

  @Test
  void testAttributeInitialization() {
    final Attribute<Integer> testAttribute = new Attribute<>(10);
    assertEquals(10, testAttribute.getModifiedValue().intValue());
  }

  @Test
  void testmodifyWithAddModificationByte() {
    // arrange
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);

    // act
    testAttributeByte.modify(new AttributeModifier<>(Modification.ADD, (byte) 100));

    // assert
    assertEquals((byte) 110, testAttributeByte.getModifiedValue().byteValue());
  }

  @Test
  void testmodifyWithAddModificationShort() {
    // arrange
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);

    // act
    testAttributeShort.modify(new AttributeModifier<>(Modification.ADD, (short) 100));

    // assert
    assertEquals((short) 110, testAttributeShort.getModifiedValue().shortValue());
  }

  @Test
  void testmodifyWithAddModificationInteger() {
    // arrange
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);

    // act
    testAttributeInt.modify(new AttributeModifier<>(Modification.ADD, 100));

    // assert
    assertEquals(110, testAttributeInt.getModifiedValue().intValue());
  }

  @Test
  void testmodifyWithAddModificationLong() {
    // arrange
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    // act
    testAttributeLong.modify(new AttributeModifier<>(Modification.ADD, 1000L));

    // assert
    assertEquals(1010L, testAttributeLong.getModifiedValue().longValue());
  }

  @Test
  void testmodifyWithAddModificationFloat() {
    // arrange
    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);

    // act
    testAttributeFloat.modify(new AttributeModifier<>(Modification.ADD, 101.1f));

    // assert
    assertEquals(111.1f, testAttributeFloat.getModifiedValue(), 0.0001f);
  }

  @Test
  void testmodifyWithAddModificationDouble() {
    // arrange
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    // act
    testAttributeDouble.modify(new AttributeModifier<>(Modification.ADD, 101.1));

    // assert
    assertEquals(111.1, testAttributeDouble.getModifiedValue(), 0.0000001);
  }


  @Test
  void testmodifyWithDivideModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modify(new AttributeModifier<>(Modification.DIVIDE, 2));
    testAttributeShort.modify(new AttributeModifier<>(Modification.DIVIDE, 2));
    testAttributeInt.modify(new AttributeModifier<>(Modification.DIVIDE, 2));
    testAttributeLong.modify(new AttributeModifier<>(Modification.DIVIDE, 2));

    testAttributeFloat.modify(new AttributeModifier<>(Modification.DIVIDE, 3));
    testAttributeDouble.modify(new AttributeModifier<>(Modification.DIVIDE, 3));

    assertEquals((byte) 5, testAttributeByte.getModifiedValue().byteValue());
    assertEquals((short) 5, testAttributeShort.getModifiedValue().shortValue());
    assertEquals(5, testAttributeInt.getModifiedValue().intValue());
    assertEquals(5L, testAttributeLong.getModifiedValue().longValue());

    assertEquals(3.333333333333f, testAttributeFloat.getModifiedValue(), 0.0001f);
    assertEquals(3.333333333333, testAttributeDouble.getModifiedValue(), 0.0000001);
  }

  @Test
  void testmodifyWithMultiplyModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modify(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testAttributeShort.modify(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testAttributeInt.modify(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testAttributeLong.modify(new AttributeModifier<>(Modification.MULTIPLY, 2));

    testAttributeFloat.modify(new AttributeModifier<>(Modification.MULTIPLY, 2.1));
    testAttributeDouble.modify(new AttributeModifier<>(Modification.MULTIPLY, 2.1));

    assertEquals((byte) 20, testAttributeByte.getModifiedValue().byteValue());
    assertEquals((short) 20, testAttributeShort.getModifiedValue().shortValue());
    assertEquals(20, testAttributeInt.getModifiedValue().intValue());
    assertEquals(20L, testAttributeLong.getModifiedValue().longValue());

    assertEquals(21f, testAttributeFloat.getModifiedValue(), 0.0001f);
    assertEquals(21, testAttributeDouble.getModifiedValue(), 0.0000001);
  }

  @Test
  void testmodifyWithSetModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modify(new AttributeModifier<>(Modification.SET, 20));
    testAttributeShort.modify(new AttributeModifier<>(Modification.SET, 20));
    testAttributeInt.modify(new AttributeModifier<>(Modification.SET, 20));
    testAttributeLong.modify(new AttributeModifier<>(Modification.SET, 20));

    testAttributeFloat.modify(new AttributeModifier<>(Modification.SET, 21));
    testAttributeDouble.modify(new AttributeModifier<>(Modification.SET, 21));

    assertEquals((byte) 20, testAttributeByte.getModifiedValue().byteValue());
    assertEquals((short) 20, testAttributeShort.getModifiedValue().shortValue());
    assertEquals(20, testAttributeInt.getModifiedValue().intValue());
    assertEquals(20L, testAttributeLong.getModifiedValue().longValue());

    assertEquals(21f, testAttributeFloat.getModifiedValue(), 0.0001f);
    assertEquals(21, testAttributeDouble.getModifiedValue(), 0.0000001);
  }

  @Test
  void testmodifyWithSubstractModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modify(new AttributeModifier<>(Modification.SUBTRACT, (byte) 1));
    testAttributeShort.modify(new AttributeModifier<>(Modification.SUBTRACT, (short) 1));
    testAttributeInt.modify(new AttributeModifier<>(Modification.SUBTRACT, 1));
    testAttributeLong.modify(new AttributeModifier<>(Modification.SUBTRACT, 1));

    testAttributeFloat.modify(new AttributeModifier<>(Modification.SUBTRACT, 0.9f));
    testAttributeDouble.modify(new AttributeModifier<>(Modification.SUBTRACT, 0.9));

    assertEquals((byte) 9, testAttributeByte.getModifiedValue().byteValue());
    assertEquals((short) 9, testAttributeShort.getModifiedValue().shortValue());
    assertEquals(9, testAttributeInt.getModifiedValue().intValue());
    assertEquals(9L, testAttributeLong.getModifiedValue().longValue());

    assertEquals(9.1f, testAttributeFloat.getModifiedValue(), 0.0001f);
    assertEquals(9.1, testAttributeDouble.getModifiedValue(), 0.0000001);
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
  void testGetModifiedValueByte() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    testAttributeByte.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    assertEquals((byte) 20, testAttributeByte.getModifiedValue().byteValue());
  }

  @Test
  void testGetModifiedValueShort() {
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    testAttributeShort.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    assertEquals((short) 20, testAttributeShort.getModifiedValue().byteValue());
  }

  @Test
  void testGetModifiedValueInteger() {
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    testAttributeInt.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    assertEquals(20, testAttributeInt.getModifiedValue().intValue());
  }

  @Test
  void testGetModifiedValueLong() {
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);
    testAttributeLong.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    assertEquals(20L, testAttributeLong.getModifiedValue().longValue());
  }

  @Test
  void testGetModifiedValueFloat() {
    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    testAttributeFloat.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    assertEquals(20.0f, testAttributeFloat.getModifiedValue().floatValue());
  }

  @Test
  void testGetModifiedValueDouble() {
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);
    testAttributeDouble.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    assertEquals(20.0d, testAttributeDouble.getModifiedValue().doubleValue());
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
  void testValueChangedEvent() {
    final Attribute<Integer> testAttribute = new Attribute<>(10);

    AtomicInteger eventFired = new AtomicInteger();
    testAttribute.addListener(_ -> eventFired.getAndIncrement());

    testAttribute.setValue(11);
    assertEquals(1, eventFired.get());

    testAttribute.modify(Modification.SET, 12);

    assertEquals(2, eventFired.get());

    final AttributeModifier<Integer> attributeModifier =
      new AttributeModifier<>(Modification.ADD, 5);

    testAttribute.addModifier(attributeModifier);
    assertEquals(3, eventFired.get());

    attributeModifier.setModifyValue(6);

    assertEquals(4, eventFired.get());

    testAttribute.removeModifier(attributeModifier);

    assertEquals(5, eventFired.get());
  }
}
