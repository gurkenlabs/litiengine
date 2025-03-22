package de.gurkenlabs.litiengine.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class RangedObservablePropertyTest {

  @Test
  void testInitRangedObservableProperty() {
    final RangedAttribute<Integer> testRangedObservablePropertyInteger = new RangedAttribute<>(10, 1, 5);
    assertEquals(5, testRangedObservablePropertyInteger.getCurrent().intValue());
    assertEquals(1, testRangedObservablePropertyInteger.getMin());
    assertEquals(10, testRangedObservablePropertyInteger.getMax());
  }

  @Test
  void testAddMinModifierWithNewModifier() {
    final RangedAttribute<Byte> testRangedObservablePropertyByte =
      new RangedAttribute<>((byte) 10, (byte) 0, (byte) 5);
    final PropertyModifier<Byte> multiplyPropertyModifier =
      new PropertyModifier<>(Modification.MULTIPLY, 2);
    final PropertyModifier<Byte> addPropertyModifier =
      new PropertyModifier<>(Modification.ADD, 20);

    assertEquals(Collections.emptyList(), testRangedObservablePropertyByte.getMinModifiers());

    testRangedObservablePropertyByte.addMinModifier(multiplyPropertyModifier);
    assertEquals(
      Collections.singletonList(multiplyPropertyModifier),
      testRangedObservablePropertyByte.getMinModifiers());

    testRangedObservablePropertyByte.addMinModifier(addPropertyModifier);
    assertEquals(
      Arrays.asList(addPropertyModifier, multiplyPropertyModifier),
      testRangedObservablePropertyByte.getMinModifiers());
  }

  @Test
  void testAddMinModifierWithExistingModifier() {
    final RangedAttribute<Byte> testRangedObservablePropertyByte =
      new RangedAttribute<>((byte) 10, (byte) 0, (byte) 5);
    final PropertyModifier<Byte> multiplyPropertyModifier =
      new PropertyModifier<>(Modification.MULTIPLY, 2);

    assertEquals(Collections.emptyList(), testRangedObservablePropertyByte.getMinModifiers());

    testRangedObservablePropertyByte.addMinModifier(multiplyPropertyModifier);
    assertEquals(
      Collections.singletonList(multiplyPropertyModifier),
      testRangedObservablePropertyByte.getMinModifiers());

    testRangedObservablePropertyByte.addMinModifier(multiplyPropertyModifier);
    assertEquals(
      Collections.singletonList(multiplyPropertyModifier),
      testRangedObservablePropertyByte.getMinModifiers());
  }

  @Test
  void testAddMaxModifierWithNewModifier() {
    final RangedAttribute<Byte> testRangedObservablePropertyByte =
      new RangedAttribute<>((byte) 10, (byte) 0, (byte) 5);
    final PropertyModifier<Byte> multiplyPropertyModifier =
      new PropertyModifier<>(Modification.MULTIPLY, 2);
    final PropertyModifier<Byte> addPropertyModifier =
      new PropertyModifier<>(Modification.ADD, 20);

    assertEquals(Collections.emptyList(), testRangedObservablePropertyByte.getMaxModifiers());

    testRangedObservablePropertyByte.addMaxModifier(multiplyPropertyModifier);
    assertEquals(
      Collections.singletonList(multiplyPropertyModifier),
      testRangedObservablePropertyByte.getMaxModifiers());

    testRangedObservablePropertyByte.addMaxModifier(addPropertyModifier);
    assertEquals(
      Arrays.asList(addPropertyModifier, multiplyPropertyModifier),
      testRangedObservablePropertyByte.getMaxModifiers());
  }

  @Test
  void testAddMaxModifierWithExistingModifier() {
    final RangedAttribute<Byte> testRangedObservablePropertyByte =
      new RangedAttribute<>((byte) 10, (byte) 0, (byte) 5);
    final PropertyModifier<Byte> multiplyPropertyModifier =
      new PropertyModifier<>(Modification.MULTIPLY, 2);

    assertEquals(Collections.emptyList(), testRangedObservablePropertyByte.getMaxModifiers());

    testRangedObservablePropertyByte.addMaxModifier(multiplyPropertyModifier);
    assertEquals(
      Collections.singletonList(multiplyPropertyModifier),
      testRangedObservablePropertyByte.getMaxModifiers());

    testRangedObservablePropertyByte.addMaxModifier(multiplyPropertyModifier);
    assertEquals(
      Collections.singletonList(multiplyPropertyModifier),
      testRangedObservablePropertyByte.getMaxModifiers());
  }

  @Test
  void testGetMin() {
    final RangedAttribute<Byte> testRangedObservablePropertyByte =
      new RangedAttribute<>((byte) 10, (byte) 1, (byte) 5);
    final RangedAttribute<Short> testRangedObservablePropertyShort =
      new RangedAttribute<>((short) 10, (short) 1, (short) 5);
    final RangedAttribute<Integer> testRangedObservablePropertyInt = new RangedAttribute<>(10, 1, 5);
    final RangedAttribute<Long> testRangedObservablePropertyLong = new RangedAttribute<>(10L, 1L, 5L);
    final RangedAttribute<Float> testRangedObservablePropertyFloat = new RangedAttribute<>(10.0f, 1.5f, 5.0f);
    final RangedAttribute<Double> testRangedObservablePropertyDouble = new RangedAttribute<>(10.0, 1.5, 5.0);

    testRangedObservablePropertyByte.addMinModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testRangedObservablePropertyShort.addMinModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testRangedObservablePropertyInt.addMinModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testRangedObservablePropertyLong.addMinModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testRangedObservablePropertyFloat.addMinModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testRangedObservablePropertyDouble.addMinModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));

    assertEquals((byte) 2, testRangedObservablePropertyByte.getMin().byteValue());
    assertEquals((short) 2, testRangedObservablePropertyShort.getMin().shortValue());
    assertEquals(2, testRangedObservablePropertyInt.getMin().intValue());
    assertEquals(2L, testRangedObservablePropertyLong.getMin().longValue());
    assertEquals(3.0f, testRangedObservablePropertyFloat.getMin().floatValue());
    assertEquals(3.0, testRangedObservablePropertyDouble.getMin().doubleValue());
  }

  @Test
  void testGetMax() {
    final RangedAttribute<Byte> testRangedObservablePropertyByte =
      new RangedAttribute<>((byte) 10, (byte) 1, (byte) 5);
    final RangedAttribute<Short> testRangedObservablePropertyShort =
      new RangedAttribute<>((short) 10, (short) 1, (short) 5);
    final RangedAttribute<Integer> testRangedObservablePropertyInt = new RangedAttribute<>(10, 1, 5);
    final RangedAttribute<Long> testRangedObservablePropertyLong = new RangedAttribute<>(10L, 1L, 5L);
    final RangedAttribute<Float> testRangedObservablePropertyFloat = new RangedAttribute<>(10.0f, 1.5f, 5.0f);
    final RangedAttribute<Double> testRangedObservablePropertyDouble = new RangedAttribute<>(10.0, 1.5, 5.0);

    testRangedObservablePropertyByte.addMaxModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testRangedObservablePropertyShort.addMaxModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testRangedObservablePropertyInt.addMaxModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testRangedObservablePropertyLong.addMaxModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testRangedObservablePropertyFloat.addMaxModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testRangedObservablePropertyDouble.addMaxModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));

    assertEquals((byte) 20, testRangedObservablePropertyByte.getMax().byteValue());
    assertEquals((short) 20, testRangedObservablePropertyShort.getMax().shortValue());
    assertEquals(20, testRangedObservablePropertyInt.getMax().intValue());
    assertEquals(20L, testRangedObservablePropertyLong.getMax().longValue());
    assertEquals(20.0f, testRangedObservablePropertyFloat.getMax().floatValue());
    assertEquals(20.0, testRangedObservablePropertyDouble.getMax().doubleValue());
  }

  @Test
  void testGetWithValueOutOfRangeMax() {
    final RangedAttribute<Byte> testRangedObservablePropertyByte =
      new RangedAttribute<>((byte) 10, (byte) 1, (byte) 5);

    testRangedObservablePropertyByte.addModifier(new PropertyModifier<>(Modification.MULTIPLY, 3));

    assertEquals((byte) 10, testRangedObservablePropertyByte.getCurrent().byteValue());

    testRangedObservablePropertyByte.addMaxModifier(new PropertyModifier<>(Modification.ADD, 10));

    assertEquals((byte) 15, testRangedObservablePropertyByte.getCurrent().byteValue());
  }

  @Test
  void testGetWithValueOutOfRangeMin() {
    final RangedAttribute<Byte> testRangedObservablePropertyByte =
      new RangedAttribute<>((byte) 10, (byte) 1, (byte) 5);

    testRangedObservablePropertyByte.addModifier(new PropertyModifier<>(Modification.SUBTRACT, 15));

    assertEquals((byte) 1, testRangedObservablePropertyByte.getCurrent().byteValue());
  }

  @Test
  void testGetRelativeCurrentValue() {
    final RangedAttribute<Byte> testRangedObservablePropertyByte =
      new RangedAttribute<>((byte) 10, (byte) 1, (byte) 5);
    final RangedAttribute<Short> testRangedObservablePropertyShort =
      new RangedAttribute<>((short) 10, (short) 1, (short) 5);
    final RangedAttribute<Integer> testRangedObservablePropertyInt = new RangedAttribute<>(10, 1, 5);
    final RangedAttribute<Long> testRangedObservablePropertyLong = new RangedAttribute<>(10L, 1L, 5L);
    final RangedAttribute<Float> testRangedObservablePropertyFloat = new RangedAttribute<>(10.0f, 1.0f, 5.0f);
    final RangedAttribute<Double> testRangedObservablePropertyDouble = new RangedAttribute<>(10.0, 1.0, 5.0);

    assertEquals(0.5, testRangedObservablePropertyByte.getRatio());
    assertEquals(0.5, testRangedObservablePropertyShort.getRatio());
    assertEquals(0.5, testRangedObservablePropertyInt.getRatio());
    assertEquals(0.5, testRangedObservablePropertyLong.getRatio());
    assertEquals(0.5, testRangedObservablePropertyFloat.getRatio());
    assertEquals(0.5, testRangedObservablePropertyDouble.getRatio());
  }

  @Test
  void testSetMaxBaseValue() {
    final RangedAttribute<Byte> testRangedObservablePropertyByte =
      new RangedAttribute<>((byte) 10, (byte) 1, (byte) 5);

    assertEquals((byte) 10, testRangedObservablePropertyByte.getMax().byteValue());

    testRangedObservablePropertyByte.setMax((byte) 20);
    assertEquals((byte) 20, testRangedObservablePropertyByte.getMax().byteValue());
  }

  @Test
  void testSetMinBaseValue() {
    final RangedAttribute<Byte> testRangedObservablePropertyByte =
      new RangedAttribute<>((byte) 50, (byte) 10, (byte) 5);

    assertEquals((byte) 10, testRangedObservablePropertyByte.getMin().byteValue());

    testRangedObservablePropertyByte.setMin((byte) 5);
    assertEquals((byte) 5, testRangedObservablePropertyByte.getMin().byteValue());
  }

  @Test
  void testmodify() {
    final RangedAttribute<Byte> testRangedObservablePropertyByte =
      new RangedAttribute<>((byte) 100, (byte) 0, (byte) 10);
    final RangedAttribute<Short> testRangedObservablePropertyShort =
      new RangedAttribute<>((short) 200, (short) 0, (short) 10);
    final RangedAttribute<Integer> testRangedObservablePropertyInt = new RangedAttribute<>(200, 0, 10);
    final RangedAttribute<Long> testRangedObservablePropertyLong = new RangedAttribute<>(2000L, 0L, 10L);
    final RangedAttribute<Float> testRangedObservablePropertyFloat = new RangedAttribute<>(200.0f, 0.0f, 10.0f);
    final RangedAttribute<Double> testRangedObservablePropertyDouble = new RangedAttribute<>(200.0, 0.0, 10.0);

    testRangedObservablePropertyByte.modify(new PropertyModifier<>(Modification.ADD, (byte) 10));
    testRangedObservablePropertyShort.modify(new PropertyModifier<>(Modification.ADD, (short) 100));
    testRangedObservablePropertyInt.modify(new PropertyModifier<>(Modification.ADD, 100));
    testRangedObservablePropertyLong.modify(new PropertyModifier<>(Modification.ADD, 1000L));
    testRangedObservablePropertyFloat.modify(new PropertyModifier<>(Modification.ADD, 101.1f));
    testRangedObservablePropertyDouble.modify(new PropertyModifier<>(Modification.ADD, 101.1));

    assertEquals((byte) 20, testRangedObservablePropertyByte.getCurrent().byteValue());
    assertEquals((short) 110, testRangedObservablePropertyShort.getCurrent().byteValue());
    assertEquals(110, testRangedObservablePropertyInt.getCurrent().intValue());
    assertEquals(1010L, testRangedObservablePropertyLong.getCurrent().longValue());
    assertEquals(111.1f, testRangedObservablePropertyFloat.getCurrent(), 0.0001f);
    assertEquals(111.1, testRangedObservablePropertyDouble.getCurrent(), 0.0000001);
  }
}
