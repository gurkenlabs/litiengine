package de.gurkenlabs.litiengine.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * The Class ObservablePropertyTests.
 */
class ObservablePropertyTests {

  @Test
  void testObservablePropertyInitialization() {
    final Attribute<Integer> testObservableProperty = new Attribute<>(10);
    assertEquals(10, testObservableProperty.getCurrent().intValue());
  }

  @Test
  void testmodifyWithAddModificationByte() {
    // arrange
    final Attribute<Byte> testObservablePropertyByte = new Attribute<>((byte) 10);

    // act
    testObservablePropertyByte.modify(new PropertyModifier<>(Modification.ADD, (byte) 100));

    // assert
    assertEquals((byte) 110, testObservablePropertyByte.getCurrent().byteValue());
  }

  @Test
  void testmodifyWithAddModificationShort() {
    // arrange
    final Attribute<Short> testObservablePropertyShort = new Attribute<>((short) 10);

    // act
    testObservablePropertyShort.modify(new PropertyModifier<>(Modification.ADD, (short) 100));

    // assert
    assertEquals((short) 110, testObservablePropertyShort.getCurrent().shortValue());
  }

  @Test
  void testmodifyWithAddModificationInteger() {
    // arrange
    final Attribute<Integer> testObservablePropertyInt = new Attribute<>(10);

    // act
    testObservablePropertyInt.modify(new PropertyModifier<>(Modification.ADD, 100));

    // assert
    assertEquals(110, testObservablePropertyInt.getCurrent().intValue());
  }

  @Test
  void testmodifyWithAddModificationLong() {
    // arrange
    final Attribute<Long> testObservablePropertyLong = new Attribute<>(10L);

    // act
    testObservablePropertyLong.modify(new PropertyModifier<>(Modification.ADD, 1000L));

    // assert
    assertEquals(1010L, testObservablePropertyLong.getCurrent().longValue());
  }

  @Test
  void testmodifyWithAddModificationFloat() {
    // arrange
    final Attribute<Float> testObservablePropertyFloat = new Attribute<>(10.0f);

    // act
    testObservablePropertyFloat.modify(new PropertyModifier<>(Modification.ADD, 101.1f));

    // assert
    assertEquals(111.1f, testObservablePropertyFloat.getCurrent(), 0.0001f);
  }

  @Test
  void testmodifyWithAddModificationDouble() {
    // arrange
    final Attribute<Double> testObservablePropertyDouble = new Attribute<>(10.0);

    // act
    testObservablePropertyDouble.modify(new PropertyModifier<>(Modification.ADD, 101.1));

    // assert
    assertEquals(111.1, testObservablePropertyDouble.getCurrent(), 0.0000001);
  }


  @Test
  void testmodifyWithDivideModification() {
    final Attribute<Byte> testObservablePropertyByte = new Attribute<>((byte) 10);
    final Attribute<Short> testObservablePropertyShort = new Attribute<>((short) 10);
    final Attribute<Integer> testObservablePropertyInt = new Attribute<>(10);
    final Attribute<Long> testObservablePropertyLong = new Attribute<>(10L);

    final Attribute<Float> testObservablePropertyFloat = new Attribute<>(10.0f);
    final Attribute<Double> testObservablePropertyDouble = new Attribute<>(10.0);

    testObservablePropertyByte.modify(new PropertyModifier<>(Modification.DIVIDE, 2));
    testObservablePropertyShort.modify(new PropertyModifier<>(Modification.DIVIDE, 2));
    testObservablePropertyInt.modify(new PropertyModifier<>(Modification.DIVIDE, 2));
    testObservablePropertyLong.modify(new PropertyModifier<>(Modification.DIVIDE, 2));

    testObservablePropertyFloat.modify(new PropertyModifier<>(Modification.DIVIDE, 3));
    testObservablePropertyDouble.modify(new PropertyModifier<>(Modification.DIVIDE, 3));

    assertEquals((byte) 5, testObservablePropertyByte.getCurrent().byteValue());
    assertEquals((short) 5, testObservablePropertyShort.getCurrent().shortValue());
    assertEquals(5, testObservablePropertyInt.getCurrent().intValue());
    assertEquals(5L, testObservablePropertyLong.getCurrent().longValue());

    assertEquals(3.333333333333f, testObservablePropertyFloat.getCurrent(), 0.0001f);
    assertEquals(3.333333333333, testObservablePropertyDouble.getCurrent(), 0.0000001);
  }

  @Test
  void testmodifyWithMultiplyModification() {
    final Attribute<Byte> testObservablePropertyByte = new Attribute<>((byte) 10);
    final Attribute<Short> testObservablePropertyShort = new Attribute<>((short) 10);
    final Attribute<Integer> testObservablePropertyInt = new Attribute<>(10);
    final Attribute<Long> testObservablePropertyLong = new Attribute<>(10L);

    final Attribute<Float> testObservablePropertyFloat = new Attribute<>(10.0f);
    final Attribute<Double> testObservablePropertyDouble = new Attribute<>(10.0);

    testObservablePropertyByte.modify(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testObservablePropertyShort.modify(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testObservablePropertyInt.modify(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testObservablePropertyLong.modify(new PropertyModifier<>(Modification.MULTIPLY, 2));

    testObservablePropertyFloat.modify(new PropertyModifier<>(Modification.MULTIPLY, 2.1));
    testObservablePropertyDouble.modify(new PropertyModifier<>(Modification.MULTIPLY, 2.1));

    assertEquals((byte) 20, testObservablePropertyByte.getCurrent().byteValue());
    assertEquals((short) 20, testObservablePropertyShort.getCurrent().shortValue());
    assertEquals(20, testObservablePropertyInt.getCurrent().intValue());
    assertEquals(20L, testObservablePropertyLong.getCurrent().longValue());

    assertEquals(21f, testObservablePropertyFloat.getCurrent(), 0.0001f);
    assertEquals(21, testObservablePropertyDouble.getCurrent(), 0.0000001);
  }

  @Test
  void testmodifyWithSetModification() {
    final Attribute<Byte> testObservablePropertyByte = new Attribute<>((byte) 10);
    final Attribute<Short> testObservablePropertyShort = new Attribute<>((short) 10);
    final Attribute<Integer> testObservablePropertyInt = new Attribute<>(10);
    final Attribute<Long> testObservablePropertyLong = new Attribute<>(10L);

    final Attribute<Float> testObservablePropertyFloat = new Attribute<>(10.0f);
    final Attribute<Double> testObservablePropertyDouble = new Attribute<>(10.0);

    testObservablePropertyByte.modify(new PropertyModifier<>(Modification.SET, 20));
    testObservablePropertyShort.modify(new PropertyModifier<>(Modification.SET, 20));
    testObservablePropertyInt.modify(new PropertyModifier<>(Modification.SET, 20));
    testObservablePropertyLong.modify(new PropertyModifier<>(Modification.SET, 20));

    testObservablePropertyFloat.modify(new PropertyModifier<>(Modification.SET, 21));
    testObservablePropertyDouble.modify(new PropertyModifier<>(Modification.SET, 21));

    assertEquals((byte) 20, testObservablePropertyByte.getCurrent().byteValue());
    assertEquals((short) 20, testObservablePropertyShort.getCurrent().shortValue());
    assertEquals(20, testObservablePropertyInt.getCurrent().intValue());
    assertEquals(20L, testObservablePropertyLong.getCurrent().longValue());

    assertEquals(21f, testObservablePropertyFloat.getCurrent(), 0.0001f);
    assertEquals(21, testObservablePropertyDouble.getCurrent(), 0.0000001);
  }

  @Test
  void testmodifyWithSubstractModification() {
    final Attribute<Byte> testObservablePropertyByte = new Attribute<>((byte) 10);
    final Attribute<Short> testObservablePropertyShort = new Attribute<>((short) 10);
    final Attribute<Integer> testObservablePropertyInt = new Attribute<>(10);
    final Attribute<Long> testObservablePropertyLong = new Attribute<>(10L);

    final Attribute<Float> testObservablePropertyFloat = new Attribute<>(10.0f);
    final Attribute<Double> testObservablePropertyDouble = new Attribute<>(10.0);

    testObservablePropertyByte.modify(new PropertyModifier<>(Modification.SUBTRACT, (byte) 1));
    testObservablePropertyShort.modify(new PropertyModifier<>(Modification.SUBTRACT, (short) 1));
    testObservablePropertyInt.modify(new PropertyModifier<>(Modification.SUBTRACT, 1));
    testObservablePropertyLong.modify(new PropertyModifier<>(Modification.SUBTRACT, 1));

    testObservablePropertyFloat.modify(new PropertyModifier<>(Modification.SUBTRACT, 0.9f));
    testObservablePropertyDouble.modify(new PropertyModifier<>(Modification.SUBTRACT, 0.9));

    assertEquals((byte) 9, testObservablePropertyByte.getCurrent().byteValue());
    assertEquals((short) 9, testObservablePropertyShort.getCurrent().shortValue());
    assertEquals(9, testObservablePropertyInt.getCurrent().intValue());
    assertEquals(9L, testObservablePropertyLong.getCurrent().longValue());

    assertEquals(9.1f, testObservablePropertyFloat.getCurrent(), 0.0001f);
    assertEquals(9.1, testObservablePropertyDouble.getCurrent(), 0.0000001);
  }

  @Test
  void testAddModifierWithNewModifier() {
    final Attribute<Byte> testObservablePropertyByte = new Attribute<>((byte) 10);
    final PropertyModifier<Byte> multiplyPropertyModifier =
      new PropertyModifier<>(Modification.MULTIPLY, 2);
    final PropertyModifier<Byte> addPropertyModifier =
      new PropertyModifier<>(Modification.ADD, 50);

    assertEquals(Collections.emptyList(), testObservablePropertyByte.getModifiers());

    testObservablePropertyByte.addModifier(multiplyPropertyModifier);
    assertEquals(
      Collections.singletonList(multiplyPropertyModifier), testObservablePropertyByte.getModifiers());

    testObservablePropertyByte.addModifier(addPropertyModifier);
    assertEquals(
      Arrays.asList(addPropertyModifier, multiplyPropertyModifier),
      testObservablePropertyByte.getModifiers());
  }

  @Test
  void testAddModifierWithExistingModifier() {
    final Attribute<Byte> testObservablePropertyByte = new Attribute<>((byte) 10);
    final PropertyModifier<Byte> addPropertyModifier =
      new PropertyModifier<>(Modification.ADD, 50);

    assertEquals(Collections.emptyList(), testObservablePropertyByte.getModifiers());

    testObservablePropertyByte.addModifier(addPropertyModifier);
    assertEquals(Collections.singletonList(addPropertyModifier), testObservablePropertyByte.getModifiers());

    testObservablePropertyByte.addModifier(addPropertyModifier);
    assertEquals(Collections.singletonList(addPropertyModifier), testObservablePropertyByte.getModifiers());
  }

  @Test
  void testRemoveModifier() {
    final Attribute<Byte> testObservablePropertyByte = new Attribute<>((byte) 10);
    final PropertyModifier<Byte> multiplyPropertyModifier =
      new PropertyModifier<>(Modification.MULTIPLY, 2);
    final PropertyModifier<Byte> addPropertyModifier =
      new PropertyModifier<>(Modification.ADD, 50);
    final PropertyModifier<Byte> subtractPropertyModifier =
      new PropertyModifier<>(Modification.SUBTRACT, 25);

    testObservablePropertyByte.addModifier(multiplyPropertyModifier);
    testObservablePropertyByte.addModifier(addPropertyModifier);
    testObservablePropertyByte.addModifier(subtractPropertyModifier);
    assertEquals(
      Arrays.asList(addPropertyModifier, subtractPropertyModifier, multiplyPropertyModifier),
      testObservablePropertyByte.getModifiers());

    testObservablePropertyByte.removeModifier(subtractPropertyModifier);
    assertEquals(
      Arrays.asList(addPropertyModifier, multiplyPropertyModifier),
      testObservablePropertyByte.getModifiers());
  }

  @Test
  void testGetByte() {
    final Attribute<Byte> testObservablePropertyByte = new Attribute<>((byte) 10);
    testObservablePropertyByte.addModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    assertEquals((byte) 20, testObservablePropertyByte.getCurrent().byteValue());
  }

  @Test
  void testGetShort() {
    final Attribute<Short> testObservablePropertyShort = new Attribute<>((short) 10);
    testObservablePropertyShort.addModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    assertEquals((short) 20, testObservablePropertyShort.getCurrent().byteValue());
  }

  @Test
  void testGetInteger() {
    final Attribute<Integer> testObservablePropertyInt = new Attribute<>(10);
    testObservablePropertyInt.addModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    assertEquals(20, testObservablePropertyInt.getCurrent().intValue());
  }

  @Test
  void testGetLong() {
    final Attribute<Long> testObservablePropertyLong = new Attribute<>(10L);
    testObservablePropertyLong.addModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    assertEquals(20L, testObservablePropertyLong.getCurrent().longValue());
  }

  @Test
  void testGetFloat() {
    final Attribute<Float> testObservablePropertyFloat = new Attribute<>(10.0f);
    testObservablePropertyFloat.addModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    assertEquals(20.0f, testObservablePropertyFloat.getCurrent().floatValue());
  }

  @Test
  void testGetDouble() {
    final Attribute<Double> testObservablePropertyDouble = new Attribute<>(10.0);
    testObservablePropertyDouble.addModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    assertEquals(20.0d, testObservablePropertyDouble.getCurrent().doubleValue());
  }

  @Test
  void testToString() {
    final Attribute<Byte> testObservablePropertyByte = new Attribute<>((byte) 10);
    final Attribute<Short> testObservablePropertyShort = new Attribute<>((short) 10);
    final Attribute<Integer> testObservablePropertyInt = new Attribute<>(10);
    final Attribute<Long> testObservablePropertyLong = new Attribute<>(10L);

    final Attribute<Float> testObservablePropertyFloat = new Attribute<>(10.0f);
    final Attribute<Double> testObservablePropertyDouble = new Attribute<>(10.0);

    testObservablePropertyByte.addModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testObservablePropertyShort.addModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testObservablePropertyInt.addModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testObservablePropertyLong.addModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testObservablePropertyFloat.addModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));
    testObservablePropertyDouble.addModifier(new PropertyModifier<>(Modification.MULTIPLY, 2));

    assertEquals("20", testObservablePropertyByte.toString());
    assertEquals("20", testObservablePropertyShort.toString());
    assertEquals("20", testObservablePropertyInt.toString());
    assertEquals("20", testObservablePropertyLong.toString());
    assertEquals("20.0", testObservablePropertyFloat.toString());
    assertEquals("20.0", testObservablePropertyDouble.toString());
  }

  @Test
  void testValueChangedEvent() {
    final Attribute<Integer> testObservableProperty = new Attribute<>(10);

    AtomicInteger eventFired = new AtomicInteger();
    testObservableProperty.addListener(_ -> eventFired.getAndIncrement());

    testObservableProperty.set(11);
    assertEquals(1, eventFired.get());

    testObservableProperty.modify(Modification.SET, 12);

    assertEquals(2, eventFired.get());

    final PropertyModifier<Integer> propertyModifier =
      new PropertyModifier<>(Modification.ADD, 5);

    testObservableProperty.addModifier(propertyModifier);
    assertEquals(3, eventFired.get());

    propertyModifier.setModifyValue(6);

    assertEquals(4, eventFired.get());

    testObservableProperty.removeModifier(propertyModifier);

    assertEquals(5, eventFired.get());
  }
}
