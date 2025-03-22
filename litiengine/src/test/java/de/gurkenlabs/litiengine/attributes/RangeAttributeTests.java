package de.gurkenlabs.litiengine.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class RangeAttributeTests {

  @Test void testInitRangedAttribute() {
    final RangeAttribute<Integer> testRangeAttributeInteger = new RangeAttribute<>(5, 1, 10);
    assertEquals(5, testRangeAttributeInteger.getModifiedValue().intValue());
    assertEquals(1, testRangeAttributeInteger.getModifiedMin());
    assertEquals(10, testRangeAttributeInteger.getModifiedMax());
  }

  @Test void testAddMinModifierWithNewModifier() {
    final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 5, (byte) 0, (byte) 10);
    final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<>(Modification.MULTIPLY, 2);
    final AttributeModifier<Byte> addAttributeModifier = new AttributeModifier<>(Modification.ADD, 20);

    assertEquals(Collections.emptyList(), testRangeAttributeByte.getMinModifiers());

    testRangeAttributeByte.addMinModifier(multiplyAttributeModifier);
    assertEquals(Collections.singletonList(multiplyAttributeModifier), testRangeAttributeByte.getMinModifiers());

    testRangeAttributeByte.addMinModifier(addAttributeModifier);
    assertEquals(Arrays.asList(addAttributeModifier, multiplyAttributeModifier), testRangeAttributeByte.getMinModifiers());
  }

  @Test void testAddMinModifierWithExistingModifier() {
    final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 5, (byte) 0, (byte) 10);
    final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<>(Modification.MULTIPLY, 2);

    assertEquals(Collections.emptyList(), testRangeAttributeByte.getMinModifiers());

    testRangeAttributeByte.addMinModifier(multiplyAttributeModifier);
    assertEquals(Collections.singletonList(multiplyAttributeModifier), testRangeAttributeByte.getMinModifiers());

    testRangeAttributeByte.addMinModifier(multiplyAttributeModifier);
    assertEquals(Collections.singletonList(multiplyAttributeModifier), testRangeAttributeByte.getMinModifiers());
  }

  @Test void testAddMaxModifierWithNewModifier() {
    final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 5, (byte) 0, (byte) 10);
    final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<>(Modification.MULTIPLY, 2);
    final AttributeModifier<Byte> addAttributeModifier = new AttributeModifier<>(Modification.ADD, 20);

    assertEquals(Collections.emptyList(), testRangeAttributeByte.getMaxModifiers());

    testRangeAttributeByte.addMaxModifier(multiplyAttributeModifier);
    assertEquals(Collections.singletonList(multiplyAttributeModifier), testRangeAttributeByte.getMaxModifiers());

    testRangeAttributeByte.addMaxModifier(addAttributeModifier);
    assertEquals(Arrays.asList(addAttributeModifier, multiplyAttributeModifier), testRangeAttributeByte.getMaxModifiers());
  }

  @Test void testAddMaxModifierWithExistingModifier() {
    final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 5, (byte) 0, (byte) 10);
    final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<>(Modification.MULTIPLY, 2);

    assertEquals(Collections.emptyList(), testRangeAttributeByte.getMaxModifiers());

    testRangeAttributeByte.addMaxModifier(multiplyAttributeModifier);
    assertEquals(Collections.singletonList(multiplyAttributeModifier), testRangeAttributeByte.getMaxModifiers());

    testRangeAttributeByte.addMaxModifier(multiplyAttributeModifier);
    assertEquals(Collections.singletonList(multiplyAttributeModifier), testRangeAttributeByte.getMaxModifiers());
  }

  @Test void testGetModifiedValueMin() {
    final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 5, (byte) 1, (byte) 10);
    final RangeAttribute<Short> testRangeAttributeShort = new RangeAttribute<>((short) 5, (short) 1, (short) 10);
    final RangeAttribute<Integer> testRangeAttributeInt = new RangeAttribute<>(5, 1, 10);
    final RangeAttribute<Long> testRangeAttributeLong = new RangeAttribute<>(5L, 1L, 10L);
    final RangeAttribute<Float> testRangeAttributeFloat = new RangeAttribute<>(5.0f, 1.5f, 10.0f);
    final RangeAttribute<Double> testRangeAttributeDouble = new RangeAttribute<>(5.0, 1.5, 10.0);

    testRangeAttributeByte.addMinModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testRangeAttributeShort.addMinModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testRangeAttributeInt.addMinModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testRangeAttributeLong.addMinModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testRangeAttributeFloat.addMinModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testRangeAttributeDouble.addMinModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));

    assertEquals((byte) 2, testRangeAttributeByte.getModifiedMin().byteValue());
    assertEquals((short) 2, testRangeAttributeShort.getModifiedMin().shortValue());
    assertEquals(2, testRangeAttributeInt.getModifiedMin().intValue());
    assertEquals(2L, testRangeAttributeLong.getModifiedMin().longValue());
    assertEquals(3.0f, testRangeAttributeFloat.getModifiedMin().floatValue());
    assertEquals(3.0, testRangeAttributeDouble.getModifiedMin().doubleValue());
  }

  @Test void testGetModifiedValueMax() {
    final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 5, (byte) 1, (byte) 10);
    final RangeAttribute<Short> testRangeAttributeShort = new RangeAttribute<>((short) 5, (short) 1, (short) 10);
    final RangeAttribute<Integer> testRangeAttributeInt = new RangeAttribute<>(5, 1, 10);
    final RangeAttribute<Long> testRangeAttributeLong = new RangeAttribute<>(5L, 1L, 10L);
    final RangeAttribute<Float> testRangeAttributeFloat = new RangeAttribute<>(5.0f, 1.5f, 10.0f);
    final RangeAttribute<Double> testRangeAttributeDouble = new RangeAttribute<>(5.0, 1.5, 10.0);

    testRangeAttributeByte.addMaxModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testRangeAttributeShort.addMaxModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testRangeAttributeInt.addMaxModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testRangeAttributeLong.addMaxModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testRangeAttributeFloat.addMaxModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));
    testRangeAttributeDouble.addMaxModifier(new AttributeModifier<>(Modification.MULTIPLY, 2));

    assertEquals((byte) 20, testRangeAttributeByte.getModifiedMax().byteValue());
    assertEquals((short) 20, testRangeAttributeShort.getModifiedMax().shortValue());
    assertEquals(20, testRangeAttributeInt.getModifiedMax().intValue());
    assertEquals(20L, testRangeAttributeLong.getModifiedMax().longValue());
    assertEquals(20.0f, testRangeAttributeFloat.getModifiedMax().floatValue());
    assertEquals(20.0, testRangeAttributeDouble.getModifiedMax().doubleValue());
  }

  @Test void testGetModifiedValueWithValueOutOfRangeMax() {
    final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 5, (byte) 1, (byte) 10);

    testRangeAttributeByte.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 3));

    assertEquals((byte) 10, testRangeAttributeByte.getModifiedValue().byteValue());

    testRangeAttributeByte.addMaxModifier(new AttributeModifier<>(Modification.ADD, 10));

    assertEquals((byte) 15, testRangeAttributeByte.getModifiedValue().byteValue());
  }

  @Test void testGetModifiedValueWithValueOutOfRangeMin() {
    final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 15, (byte) 5, (byte) 20);

    testRangeAttributeByte.addModifier(new AttributeModifier<>(Modification.SUBTRACT, 15));

    assertEquals((byte) 5, testRangeAttributeByte.getModifiedValue().byteValue());
  }

  @Test void testGetModifiedValueRelativeCurrentValue() {
    final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 1, (byte) 5, (byte) 10);
    final RangeAttribute<Short> testRangeAttributeShort = new RangeAttribute<>((short) 1, (short) 5, (short) 10);
    final RangeAttribute<Integer> testRangeAttributeInt = new RangeAttribute<>(1, 5, 10);
    final RangeAttribute<Long> testRangeAttributeLong = new RangeAttribute<>(1L, 5L, 10L);
    final RangeAttribute<Float> testRangeAttributeFloat = new RangeAttribute<>(1.0f, 5.0f, 10.0f);
    final RangeAttribute<Double> testRangeAttributeDouble = new RangeAttribute<>(1.0, 5.0, 10.0);

    assertEquals(0.5, testRangeAttributeByte.getRatio());
    assertEquals(0.5, testRangeAttributeShort.getRatio());
    assertEquals(0.5, testRangeAttributeInt.getRatio());
    assertEquals(0.5, testRangeAttributeLong.getRatio());
    assertEquals(0.5, testRangeAttributeFloat.getRatio());
    assertEquals(0.5, testRangeAttributeDouble.getRatio());
  }

  @Test void testSetValueMaxBaseValue() {
    final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 8, (byte) 5, (byte) 10);

    assertEquals((byte) 10, testRangeAttributeByte.getModifiedMax().byteValue());

    testRangeAttributeByte.setMax((byte) 7);
    assertEquals((byte) 7, testRangeAttributeByte.getModifiedMax().byteValue());
    assertEquals((byte) 7, testRangeAttributeByte.getModifiedValue().byteValue());
  }

  @Test void testSetValueMinBaseValue() {
    final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 10, (byte) 5, (byte) 50);

    assertEquals((byte) 5, testRangeAttributeByte.getModifiedMin().byteValue());

    testRangeAttributeByte.setMin((byte) 15);
    assertEquals((byte) 15, testRangeAttributeByte.getModifiedMin().byteValue());
    assertEquals((byte) 15, testRangeAttributeByte.getModifiedValue().byteValue());
  }

  @Test void testModifyAdd() {
    final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 0, (byte) 10, (byte) 100);
    final RangeAttribute<Short> testRangeAttributeShort = new RangeAttribute<>((short) 0, (short) 10, (short) 200);
    final RangeAttribute<Integer> testRangeAttributeInt = new RangeAttribute<>(0, 10, 200);
    final RangeAttribute<Long> testRangeAttributeLong = new RangeAttribute<>(0L, 10L, 2000L);
    final RangeAttribute<Float> testRangeAttributeFloat = new RangeAttribute<>(0.0f, 10.0f, 200.0f);
    final RangeAttribute<Double> testRangeAttributeDouble = new RangeAttribute<>(0.0, 10.0, 200.0);

    testRangeAttributeByte.modify(new AttributeModifier<>(Modification.ADD, (byte) 10));
    testRangeAttributeShort.modify(new AttributeModifier<>(Modification.ADD, (short) 100));
    testRangeAttributeInt.modify(new AttributeModifier<>(Modification.ADD, 100));
    testRangeAttributeLong.modify(new AttributeModifier<>(Modification.ADD, 1000L));
    testRangeAttributeFloat.modify(new AttributeModifier<>(Modification.ADD, 101.1f));
    testRangeAttributeDouble.modify(new AttributeModifier<>(Modification.ADD, 101.1));

    assertEquals((byte) 20, testRangeAttributeByte.getModifiedValue().byteValue());
    assertEquals((short) 110, testRangeAttributeShort.getModifiedValue().byteValue());
    assertEquals(110, testRangeAttributeInt.getModifiedValue().intValue());
    assertEquals(1010L, testRangeAttributeLong.getModifiedValue().longValue());
    assertEquals(111.1f, testRangeAttributeFloat.getModifiedValue(), 0.0001f);
    assertEquals(111.1, testRangeAttributeDouble.getModifiedValue(), 0.0000001);
  }
}
