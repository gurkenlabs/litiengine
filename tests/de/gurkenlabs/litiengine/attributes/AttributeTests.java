package de.gurkenlabs.litiengine.attributes;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The Class AttributeTests.
 */
public class AttributeTests {

  @Test
  public void testAttributeInitialization() {
    final Attribute<Integer> testAttribute = new Attribute<>(10);
    assertEquals(10, testAttribute.get().intValue());
  }

  @Test
  public void testModifyBaseValueWithAddModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modifyBaseValue(new AttributeModifier<Byte>(Modification.ADD, (byte) 100));
    testAttributeShort.modifyBaseValue(new AttributeModifier<Short>(Modification.ADD, (short) 100));
    testAttributeInt.modifyBaseValue(new AttributeModifier<Integer>(Modification.ADD, 100));
    testAttributeLong.modifyBaseValue(new AttributeModifier<Long>(Modification.ADD, 1000L));

    testAttributeFloat.modifyBaseValue(new AttributeModifier<Float>(Modification.ADD, 101.1f));
    testAttributeDouble.modifyBaseValue(new AttributeModifier<Double>(Modification.ADD, 101.1));

    assertEquals((byte) 110, testAttributeByte.get().byteValue());
    assertEquals((short) 110, testAttributeShort.get().shortValue());
    assertEquals(110, testAttributeInt.get().intValue());
    assertEquals(1010L, testAttributeLong.get().longValue());

    assertEquals(111.1f, testAttributeFloat.get().floatValue(), 0.0001f);
    assertEquals(111.1, testAttributeDouble.get().doubleValue(), 0.0000001);
  }

  @Test
  public void testModifyBaseValueWithAddPercentModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modifyBaseValue(new AttributeModifier<Byte>(Modification.ADDPERCENT, 10));
    testAttributeShort.modifyBaseValue(new AttributeModifier<Short>(Modification.ADDPERCENT, 10));
    testAttributeInt.modifyBaseValue(new AttributeModifier<Integer>(Modification.ADDPERCENT, 10));
    testAttributeLong.modifyBaseValue(new AttributeModifier<Long>(Modification.ADDPERCENT, 10));

    testAttributeFloat.modifyBaseValue(new AttributeModifier<Float>(Modification.ADDPERCENT, 11));
    testAttributeDouble.modifyBaseValue(new AttributeModifier<Double>(Modification.ADDPERCENT, 11));

    assertEquals((byte) 11, testAttributeByte.get().byteValue());
    assertEquals((short) 11, testAttributeShort.get().shortValue());
    assertEquals(11, testAttributeInt.get().intValue());
    assertEquals(11L, testAttributeLong.get().longValue());

    assertEquals(11.1f, testAttributeFloat.get().floatValue(), 0.0001f);
    assertEquals(11.1, testAttributeDouble.get().doubleValue(), 0.0000001);
  }

  @Test
  public void testModifyBaseValueWithDivideModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modifyBaseValue(new AttributeModifier<Byte>(Modification.DIVIDE, 2));
    testAttributeShort.modifyBaseValue(new AttributeModifier<Short>(Modification.DIVIDE, 2));
    testAttributeInt.modifyBaseValue(new AttributeModifier<Integer>(Modification.DIVIDE, 2));
    testAttributeLong.modifyBaseValue(new AttributeModifier<Long>(Modification.DIVIDE, 2));

    testAttributeFloat.modifyBaseValue(new AttributeModifier<Float>(Modification.DIVIDE, 3));
    testAttributeDouble.modifyBaseValue(new AttributeModifier<Double>(Modification.DIVIDE, 3));

    assertEquals((byte) 5, testAttributeByte.get().byteValue());
    assertEquals((short) 5, testAttributeShort.get().shortValue());
    assertEquals(5, testAttributeInt.get().intValue());
    assertEquals(5L, testAttributeLong.get().longValue());

    assertEquals(3.333333333333f, testAttributeFloat.get().floatValue(), 0.0001f);
    assertEquals(3.333333333333, testAttributeDouble.get().doubleValue(), 0.0000001);
  }

  @Test
  public void testModifyBaseValueWithMultiplyModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modifyBaseValue(new AttributeModifier<Byte>(Modification.MULTIPLY, 2));
    testAttributeShort.modifyBaseValue(new AttributeModifier<Short>(Modification.MULTIPLY, 2));
    testAttributeInt.modifyBaseValue(new AttributeModifier<Integer>(Modification.MULTIPLY, 2));
    testAttributeLong.modifyBaseValue(new AttributeModifier<Long>(Modification.MULTIPLY, 2));

    testAttributeFloat.modifyBaseValue(new AttributeModifier<Float>(Modification.MULTIPLY, 2.1));
    testAttributeDouble.modifyBaseValue(new AttributeModifier<Double>(Modification.MULTIPLY, 2.1));

    assertEquals((byte) 20, testAttributeByte.get().byteValue());
    assertEquals((short) 20, testAttributeShort.get().shortValue());
    assertEquals(20, testAttributeInt.get().intValue());
    assertEquals(20L, testAttributeLong.get().longValue());

    assertEquals(21f, testAttributeFloat.get().floatValue(), 0.0001f);
    assertEquals(21, testAttributeDouble.get().doubleValue(), 0.0000001);
  }

  @Test
  public void testModifyBaseValueWithSetModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modifyBaseValue(new AttributeModifier<Byte>(Modification.SET, 20));
    testAttributeShort.modifyBaseValue(new AttributeModifier<Short>(Modification.SET, 20));
    testAttributeInt.modifyBaseValue(new AttributeModifier<Integer>(Modification.SET, 20));
    testAttributeLong.modifyBaseValue(new AttributeModifier<Long>(Modification.SET, 20));

    testAttributeFloat.modifyBaseValue(new AttributeModifier<Float>(Modification.SET, 21));
    testAttributeDouble.modifyBaseValue(new AttributeModifier<Double>(Modification.SET, 21));

    assertEquals((byte) 20, testAttributeByte.get().byteValue());
    assertEquals((short) 20, testAttributeShort.get().shortValue());
    assertEquals(20, testAttributeInt.get().intValue());
    assertEquals(20L, testAttributeLong.get().longValue());

    assertEquals(21f, testAttributeFloat.get().floatValue(), 0.0001f);
    assertEquals(21, testAttributeDouble.get().doubleValue(), 0.0000001);
  }

  @Test
  public void testModifyBaseValueWithSubstractModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modifyBaseValue(new AttributeModifier<Byte>(Modification.SUBTRACT, (byte) 1));
    testAttributeShort.modifyBaseValue(new AttributeModifier<Short>(Modification.SUBTRACT, (short) 1));
    testAttributeInt.modifyBaseValue(new AttributeModifier<Integer>(Modification.SUBTRACT, 1));
    testAttributeLong.modifyBaseValue(new AttributeModifier<Long>(Modification.SUBTRACT, 1));

    testAttributeFloat.modifyBaseValue(new AttributeModifier<Float>(Modification.SUBTRACT, 0.9f));
    testAttributeDouble.modifyBaseValue(new AttributeModifier<Double>(Modification.SUBTRACT, 0.9));

    assertEquals((byte) 9, testAttributeByte.get().byteValue());
    assertEquals((short) 9, testAttributeShort.get().shortValue());
    assertEquals(9, testAttributeInt.get().intValue());
    assertEquals(9L, testAttributeLong.get().longValue());

    assertEquals(9.1f, testAttributeFloat.get().floatValue(), 0.0001f);
    assertEquals(9.1, testAttributeDouble.get().doubleValue(), 0.0000001);
  }

  @Test
  public void testModifyBaseValueWithSubstractPercentModification() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final Attribute<Short> testAttributeShort = new Attribute<>((short) 10);
    final Attribute<Integer> testAttributeInt = new Attribute<>(10);
    final Attribute<Long> testAttributeLong = new Attribute<>(10L);

    final Attribute<Float> testAttributeFloat = new Attribute<>(10.0f);
    final Attribute<Double> testAttributeDouble = new Attribute<>(10.0);

    testAttributeByte.modifyBaseValue(new AttributeModifier<Byte>(Modification.SUBTRACTPERCENT, 10));
    testAttributeShort.modifyBaseValue(new AttributeModifier<Short>(Modification.SUBTRACTPERCENT, 10));
    testAttributeInt.modifyBaseValue(new AttributeModifier<Integer>(Modification.SUBTRACTPERCENT, 10));
    testAttributeLong.modifyBaseValue(new AttributeModifier<Long>(Modification.SUBTRACTPERCENT, 10));

    testAttributeFloat.modifyBaseValue(new AttributeModifier<Float>(Modification.SUBTRACTPERCENT, 9));
    testAttributeDouble.modifyBaseValue(new AttributeModifier<Double>(Modification.SUBTRACTPERCENT, 9));

    assertEquals((byte) 9, testAttributeByte.get().byteValue());
    assertEquals((short) 9, testAttributeShort.get().shortValue());
    assertEquals(9, testAttributeInt.get().intValue());
    assertEquals(9L, testAttributeLong.get().longValue());

    assertEquals(9.1f, testAttributeFloat.get().floatValue(), 0.0001f);
    assertEquals(9.1, testAttributeDouble.get().doubleValue(), 0.0000001);
  }

  @Test
  public void testModifyBaseValueRangeAttributeMaxModification() {
    final RangeAttribute<Byte> testAttributeByte = new RangeAttribute<>((byte) 10, (byte) 0, (byte) 10);
    final RangeAttribute<Short> testAttributeShort = new RangeAttribute<>((short) 10, (short) 0, (short) 10);
    final RangeAttribute<Integer> testAttributeInt = new RangeAttribute<>(10, 0, 10);
    final RangeAttribute<Long> testAttributeLong = new RangeAttribute<>(10L, 0L, 10L);

    final RangeAttribute<Float> testAttributeFloat = new RangeAttribute<>(10F, 0F, 10F);
    final RangeAttribute<Double> testAttributeDouble = new RangeAttribute<>(10.0, 0.0, 10.0);
    
    testAttributeByte.modifyMaxBaseValue(new AttributeModifier<Byte>(Modification.SUBTRACTPERCENT, 10));
    testAttributeShort.modifyMaxBaseValue(new AttributeModifier<Short>(Modification.SUBTRACTPERCENT, 10));
    testAttributeInt.modifyMaxBaseValue(new AttributeModifier<Integer>(Modification.SUBTRACTPERCENT, 10));
    testAttributeLong.modifyMaxBaseValue(new AttributeModifier<Long>(Modification.SUBTRACTPERCENT, 10));

    testAttributeFloat.modifyMaxBaseValue(new AttributeModifier<Float>(Modification.SUBTRACTPERCENT, 9));
    testAttributeDouble.modifyMaxBaseValue(new AttributeModifier<Double>(Modification.SUBTRACTPERCENT, 9));
    
    assertEquals((byte) 9, testAttributeByte.getMax().byteValue());
    assertEquals((short) 9, testAttributeShort.getMax().shortValue());
    assertEquals(9, testAttributeInt.getMax().intValue());
    assertEquals(9L, testAttributeLong.getMax().longValue());

    assertEquals(9.1f, testAttributeFloat.getMax().floatValue(), 0.0001f);
    assertEquals(9.1, testAttributeDouble.getMax().doubleValue(), 0.0000001);
    
    testAttributeByte.addMaxModifier(new AttributeModifier<Byte>(Modification.ADDPERCENT, 50));
    testAttributeShort.addMaxModifier(new AttributeModifier<Short>(Modification.ADDPERCENT, 50));
    testAttributeInt.addMaxModifier(new AttributeModifier<Integer>(Modification.ADDPERCENT, 50));
    testAttributeLong.addMaxModifier(new AttributeModifier<Long>(Modification.ADDPERCENT, 50));

    testAttributeFloat.addMaxModifier(new AttributeModifier<Float>(Modification.ADDPERCENT, 50));
    testAttributeDouble.addMaxModifier(new AttributeModifier<Double>(Modification.ADDPERCENT, 50));
    
    assertEquals((byte) 13, testAttributeByte.getMax().byteValue());
    assertEquals((short) 13, testAttributeShort.getMax().shortValue());
    assertEquals(13, testAttributeInt.getMax().intValue());
    assertEquals(13L, testAttributeLong.getMax().longValue());

    assertEquals(13.65f, testAttributeFloat.getMax().floatValue(), 0.0001f);
    assertEquals(13.65, testAttributeDouble.getMax().doubleValue(), 0.0000001);
  }

  @Test
  void testAddModifierWithNewModifier() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<Byte>(Modification.MULTIPLY, 2);
    final AttributeModifier<Byte> addAttributeModifier = new AttributeModifier<Byte>(Modification.ADD, 50);

    assertEquals(Collections.emptyList(), testAttributeByte.getModifiers());

    testAttributeByte.addModifier(multiplyAttributeModifier);
    assertEquals(List.of(multiplyAttributeModifier), testAttributeByte.getModifiers());

    testAttributeByte.addModifier(addAttributeModifier);
    assertEquals(List.of(addAttributeModifier, multiplyAttributeModifier), testAttributeByte.getModifiers());
  }

  @Test
  void testAddModifierWithExistingModifier() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final AttributeModifier<Byte> addAttributeModifier = new AttributeModifier<Byte>(Modification.ADD, 50);

    assertEquals(Collections.emptyList(), testAttributeByte.getModifiers());

    testAttributeByte.addModifier(addAttributeModifier);
    assertEquals(List.of(addAttributeModifier), testAttributeByte.getModifiers());

    testAttributeByte.addModifier(addAttributeModifier);
    assertEquals(List.of(addAttributeModifier), testAttributeByte.getModifiers());
  }

  @Test
  void testRemoveModifier() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<Byte>(Modification.MULTIPLY, 2);
    final AttributeModifier<Byte> addAttributeModifier = new AttributeModifier<Byte>(Modification.ADD, 50);
    final AttributeModifier<Byte> subtractAttributeModifier = new AttributeModifier<Byte>(Modification.SUBTRACT, 25);

    testAttributeByte.addModifier(multiplyAttributeModifier);
    testAttributeByte.addModifier(addAttributeModifier);
    testAttributeByte.addModifier(subtractAttributeModifier);
    assertEquals(List.of(addAttributeModifier, subtractAttributeModifier, multiplyAttributeModifier), testAttributeByte.getModifiers());

    testAttributeByte.removeModifier(subtractAttributeModifier);
    assertEquals(List.of(addAttributeModifier, multiplyAttributeModifier), testAttributeByte.getModifiers());
  }

  @Test
  void testIsModifierApplied() {
    final Attribute<Byte> testAttributeByte = new Attribute<>((byte) 10);
    final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<Byte>(Modification.MULTIPLY, 2);
    final AttributeModifier<Byte> addAttributeModifier = new AttributeModifier<Byte>(Modification.ADD, 50);
    final AttributeModifier<Byte> subtractAttributeModifier = new AttributeModifier<Byte>(Modification.SUBTRACT, 25);

    testAttributeByte.addModifier(multiplyAttributeModifier);
    testAttributeByte.addModifier(addAttributeModifier);
    assertEquals(List.of(addAttributeModifier, multiplyAttributeModifier), testAttributeByte.getModifiers());

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

    testAttributeByte.addModifier(new AttributeModifier<Byte>(Modification.MULTIPLY, 2));
    testAttributeShort.addModifier(new AttributeModifier<Short>(Modification.MULTIPLY, 2));
    testAttributeInt.addModifier(new AttributeModifier<Integer>(Modification.MULTIPLY, 2));
    testAttributeLong.addModifier(new AttributeModifier<Long>(Modification.MULTIPLY, 2));
    testAttributeFloat.addModifier(new AttributeModifier<Float>(Modification.MULTIPLY, 2));
    testAttributeDouble.addModifier(new AttributeModifier<Double>(Modification.MULTIPLY, 2));

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

    testAttributeByte.addModifier(new AttributeModifier<Byte>(Modification.MULTIPLY, 2));
    testAttributeShort.addModifier(new AttributeModifier<Short>(Modification.MULTIPLY, 2));
    testAttributeInt.addModifier(new AttributeModifier<Integer>(Modification.MULTIPLY, 2));
    testAttributeLong.addModifier(new AttributeModifier<Long>(Modification.MULTIPLY, 2));
    testAttributeFloat.addModifier(new AttributeModifier<Float>(Modification.MULTIPLY, 2));
    testAttributeDouble.addModifier(new AttributeModifier<Double>(Modification.MULTIPLY, 2));

    assertEquals("20", testAttributeByte.toString());
    assertEquals("20", testAttributeShort.toString());
    assertEquals("20", testAttributeInt.toString());
    assertEquals("20", testAttributeLong.toString());
    assertEquals("20.0", testAttributeFloat.toString());
    assertEquals("20.0", testAttributeDouble.toString());
  }
}
