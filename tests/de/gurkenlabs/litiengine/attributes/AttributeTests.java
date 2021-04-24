package de.gurkenlabs.litiengine.attributes;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    testAttributeByte.modifyBaseValue(new AttributeModifier(Modification.ADD, (byte) 100));

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
    assertEquals(111.1f, testAttributeFloat.get().floatValue(), 0.0001f);
  }

  @Test
  void testModifyBaseValueWithAddModificationDouble() {
    // arrange
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    // act
    testAttributeDouble.modifyBaseValue(new AttributeModifier<>(Modification.ADD, 101.1));

    // assert
    assertEquals(111.1, testAttributeDouble.get().doubleValue(), 0.0000001);
  }

  @Test
  void testModifyBaseValueWithAddPercentModificationByte() {
    // arrange
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);

    // act
    testAttributeByte.modifyBaseValue(new AttributeModifier<Byte>(Modification.ADDPERCENT, 10));

    // assert
    assertEquals((byte) 11, testAttributeByte.get().byteValue());
  }

  @Test
  void testModifyBaseValueWithAddPercentModificationShort() {
    // arrange
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);

    // act
    testAttributeShort.modifyBaseValue(new AttributeModifier<Short>(Modification.ADDPERCENT, 10));

    // assert
    assertEquals((short) 11, testAttributeShort.get().shortValue());
  }

  @Test
  void testModifyBaseValueWithAddPercentModificationInteger() {
    // arrange
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);

    // act
    testAttributeInt.modifyBaseValue(new AttributeModifier<Integer>(Modification.ADDPERCENT, 10));

    // assert
    assertEquals(11, testAttributeInt.get().intValue());
  }

  @Test
  void testModifyBaseValueWithAddPercentModificationLong() {
    // arrange
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    // act
    testAttributeLong.modifyBaseValue(new AttributeModifier<Long>(Modification.ADDPERCENT, 10));

    // assert
    assertEquals(11L, testAttributeLong.get().longValue());
  }

  @Test
  void testModifyBaseValueWithAddPercentModificationFloat() {
    // arrange
    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);

    // act
    testAttributeFloat.modifyBaseValue(new AttributeModifier<Float>(Modification.ADDPERCENT, 11));

    // assert
    assertEquals(11.1f, testAttributeFloat.get().floatValue(), 0.0001f);
  }

  @Test
  void testModifyBaseValueWithAddPercentModificationDouble() {
    // arrange
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    // act
    testAttributeDouble.modifyBaseValue(new AttributeModifier<Double>(Modification.ADDPERCENT, 11));

    // assert
    assertEquals(11.1, testAttributeDouble.get().doubleValue(), 0.0000001);
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
  void testModifyBaseValueWithSubstractPercentModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACTPERCENT, 10));
    testAttributeShort.modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACTPERCENT, 10));
    testAttributeInt.modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACTPERCENT, 10));
    testAttributeLong.modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACTPERCENT, 10));

    testAttributeFloat.modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACTPERCENT, 9));
    testAttributeDouble.modifyBaseValue(new AttributeModifier<>(Modification.SUBTRACTPERCENT, 9));

    assertEquals((byte) 9, testAttributeByte.get().byteValue());
    assertEquals((short) 9, testAttributeShort.get().shortValue());
    assertEquals(9, testAttributeInt.get().intValue());
    assertEquals(9L, testAttributeLong.get().longValue());

    assertEquals(9.1f, testAttributeFloat.get(), 0.0001f);
    assertEquals(9.1, testAttributeDouble.get(), 0.0000001);
  }

  @Test
  void testModifyBaseValueRangeAttributeMaxModification() {
    final RangeAttribute<Byte> testAttributeByte = new RangeAttribute<>((byte) 10, (byte) 0, (byte) 10);
    final RangeAttribute<Short> testAttributeShort = new RangeAttribute<>((short) 10, (short) 0, (short) 10);
    final RangeAttribute<Integer> testAttributeInt = new RangeAttribute<>(10, 0, 10);
    final RangeAttribute<Long> testAttributeLong = new RangeAttribute<>(10L, 0L, 10L);

    final RangeAttribute<Float> testAttributeFloat = new RangeAttribute<>(10F, 0F, 10F);
    final RangeAttribute<Double> testAttributeDouble = new RangeAttribute<>(10.0, 0.0, 10.0);

    testAttributeByte.modifyMaxBaseValue(new AttributeModifier<>(Modification.SUBTRACTPERCENT, 10));
    testAttributeShort.modifyMaxBaseValue(new AttributeModifier<>(Modification.SUBTRACTPERCENT, 10));
    testAttributeInt.modifyMaxBaseValue(new AttributeModifier<>(Modification.SUBTRACTPERCENT, 10));
    testAttributeLong.modifyMaxBaseValue(new AttributeModifier<>(Modification.SUBTRACTPERCENT, 10));

    testAttributeFloat.modifyMaxBaseValue(new AttributeModifier<>(Modification.SUBTRACTPERCENT, 9));
    testAttributeDouble.modifyMaxBaseValue(new AttributeModifier<>(Modification.SUBTRACTPERCENT, 9));

    assertEquals((byte) 9, testAttributeByte.getMax().byteValue());
    assertEquals((short) 9, testAttributeShort.getMax().shortValue());
    assertEquals(9, testAttributeInt.getMax().intValue());
    assertEquals(9L, testAttributeLong.getMax().longValue());

    assertEquals(9.1f, testAttributeFloat.getMax(), 0.0001f);
    assertEquals(9.1, testAttributeDouble.getMax(), 0.0000001);

    testAttributeByte.addMaxModifier(new AttributeModifier<>(Modification.ADDPERCENT, 50));
    testAttributeShort.addMaxModifier(new AttributeModifier<>(Modification.ADDPERCENT, 50));
    testAttributeInt.addMaxModifier(new AttributeModifier<>(Modification.ADDPERCENT, 50));
    testAttributeLong.addMaxModifier(new AttributeModifier<>(Modification.ADDPERCENT, 50));

    testAttributeFloat.addMaxModifier(new AttributeModifier<>(Modification.ADDPERCENT, 50));
    testAttributeDouble.addMaxModifier(new AttributeModifier<>(Modification.ADDPERCENT, 50));

    assertEquals((byte) 13, testAttributeByte.getMax().byteValue());
    assertEquals((short) 13, testAttributeShort.getMax().shortValue());
    assertEquals(13, testAttributeInt.getMax().intValue());
    assertEquals(13L, testAttributeLong.getMax().longValue());

    assertEquals(13.65f, testAttributeFloat.getMax(), 0.0001f);
    assertEquals(13.65, testAttributeDouble.getMax(), 0.0000001);
  }

  @Test
  void testAddModifierWithNewModifier() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<>(Modification.MULTIPLY, 2);
    final AttributeModifier<Byte> addAttributeModifier = new AttributeModifier<>(Modification.ADD, 50);

    assertEquals(Collections.emptyList(), testAttributeByte.getModifiers());

    testAttributeByte.addModifier(multiplyAttributeModifier);
    assertEquals(Collections.singletonList(multiplyAttributeModifier), testAttributeByte.getModifiers());

    testAttributeByte.addModifier(addAttributeModifier);
    assertEquals(Arrays.asList(addAttributeModifier, multiplyAttributeModifier), testAttributeByte.getModifiers());
  }

  @Test
  void testAddModifierWithExistingModifier() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final AttributeModifier<Byte> addAttributeModifier = new AttributeModifier<>(Modification.ADD, 50);

    assertEquals(Collections.emptyList(), testAttributeByte.getModifiers());

    testAttributeByte.addModifier(addAttributeModifier);
    assertEquals(Collections.singletonList(addAttributeModifier), testAttributeByte.getModifiers());

    testAttributeByte.addModifier(addAttributeModifier);
    assertEquals(Collections.singletonList(addAttributeModifier), testAttributeByte.getModifiers());
  }

  @Test
  void testRemoveModifier() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<>(Modification.MULTIPLY, 2);
    final AttributeModifier<Byte> addAttributeModifier = new AttributeModifier<>(Modification.ADD, 50);
    final AttributeModifier<Byte> subtractAttributeModifier = new AttributeModifier<>(Modification.SUBTRACT, 25);

    testAttributeByte.addModifier(multiplyAttributeModifier);
    testAttributeByte.addModifier(addAttributeModifier);
    testAttributeByte.addModifier(subtractAttributeModifier);
    assertEquals(Arrays.asList(addAttributeModifier, subtractAttributeModifier, multiplyAttributeModifier), testAttributeByte.getModifiers());

    testAttributeByte.removeModifier(subtractAttributeModifier);
    assertEquals(Arrays.asList(addAttributeModifier, multiplyAttributeModifier), testAttributeByte.getModifiers());
  }

  @Test
  void testIsModifierApplied() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<>(Modification.MULTIPLY, 2);
    final AttributeModifier<Byte> addAttributeModifier = new AttributeModifier<>(Modification.ADD, 50);
    final AttributeModifier<Byte> subtractAttributeModifier = new AttributeModifier<>(Modification.SUBTRACT, 25);

    testAttributeByte.addModifier(multiplyAttributeModifier);
    testAttributeByte.addModifier(addAttributeModifier);
    assertEquals(Arrays.asList(addAttributeModifier, multiplyAttributeModifier), testAttributeByte.getModifiers());

    assertTrue(testAttributeByte.isModifierApplied(addAttributeModifier));
    assertTrue(testAttributeByte.isModifierApplied(multiplyAttributeModifier));
    assertFalse(testAttributeByte.isModifierApplied(subtractAttributeModifier));
  }

  @Test
  void testGet() {
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

    assertEquals((byte) 20, testAttributeByte.get().byteValue());
    assertEquals((short) 20, testAttributeShort.get().byteValue());
    assertEquals(20, testAttributeInt.get().intValue());
    assertEquals(20L, testAttributeLong.get().longValue());
    assertEquals(20.0f, testAttributeFloat.get().floatValue());
    assertEquals((byte) 20.0, testAttributeDouble.get().doubleValue());
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
}
