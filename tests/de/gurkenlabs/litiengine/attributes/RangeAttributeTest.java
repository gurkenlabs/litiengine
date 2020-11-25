package de.gurkenlabs.litiengine.attributes;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RangeAttributeTest {

    @Test
    void testInitRangeAttribute() {
        final RangeAttribute<Integer> testRangeAttributeInteger = new RangeAttribute<>(10, 1, 5);
        assertEquals(5, testRangeAttributeInteger.get().intValue());
        assertEquals(1, testRangeAttributeInteger.getMin());
        assertEquals(10, testRangeAttributeInteger.getMax());
    }

    @Test
    void testAddMinModifierWithNewModifier() {
        final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 10, (byte) 0, (byte) 5);
        final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<>(Modification.MULTIPLY, 2);
        final AttributeModifier<Byte> addAttributeModifier = new AttributeModifier<>(Modification.ADD, 20);

        assertEquals(Collections.emptyList(), testRangeAttributeByte.getMinModifiers());

        testRangeAttributeByte.addMinModifier(multiplyAttributeModifier);
        assertEquals(List.of(multiplyAttributeModifier), testRangeAttributeByte.getMinModifiers());

        testRangeAttributeByte.addMinModifier(addAttributeModifier);
        assertEquals(List.of(addAttributeModifier, multiplyAttributeModifier), testRangeAttributeByte.getMinModifiers());
    }

    @Test
    void testAddMinModifierWithExistingModifier() {
        final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 10, (byte) 0, (byte) 5);
        final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<>(Modification.MULTIPLY, 2);

        assertEquals(Collections.emptyList(), testRangeAttributeByte.getMinModifiers());

        testRangeAttributeByte.addMinModifier(multiplyAttributeModifier);
        assertEquals(List.of(multiplyAttributeModifier), testRangeAttributeByte.getMinModifiers());

        testRangeAttributeByte.addMinModifier(multiplyAttributeModifier);
        assertEquals(List.of(multiplyAttributeModifier), testRangeAttributeByte.getMinModifiers());
    }

    @Test
    void testAddMaxModifierWithNewModifier() {
        final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 10, (byte) 0, (byte) 5);
        final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<>(Modification.MULTIPLY, 2);
        final AttributeModifier<Byte> addAttributeModifier = new AttributeModifier<>(Modification.ADD, 20);

        assertEquals(Collections.emptyList(), testRangeAttributeByte.getMaxModifiers());

        testRangeAttributeByte.addMaxModifier(multiplyAttributeModifier);
        assertEquals(List.of(multiplyAttributeModifier), testRangeAttributeByte.getMaxModifiers());

        testRangeAttributeByte.addMaxModifier(addAttributeModifier);
        assertEquals(List.of(addAttributeModifier, multiplyAttributeModifier), testRangeAttributeByte.getMaxModifiers());
    }

    @Test
    void testAddMaxModifierWithExistingModifier() {
        final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 10, (byte) 0, (byte) 5);
        final AttributeModifier<Byte> multiplyAttributeModifier = new AttributeModifier<>(Modification.MULTIPLY, 2);

        assertEquals(Collections.emptyList(), testRangeAttributeByte.getMaxModifiers());

        testRangeAttributeByte.addMaxModifier(multiplyAttributeModifier);
        assertEquals(List.of(multiplyAttributeModifier), testRangeAttributeByte.getMaxModifiers());

        testRangeAttributeByte.addMaxModifier(multiplyAttributeModifier);
        assertEquals(List.of(multiplyAttributeModifier), testRangeAttributeByte.getMaxModifiers());
    }

    @Test
    void testSetToMin() {
        final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 10, (byte) 0, (byte) 5);

        assertEquals((byte) 5, testRangeAttributeByte.get().byteValue());

        testRangeAttributeByte.setToMin();

        assertEquals((byte) 0, testRangeAttributeByte.get().byteValue());
    }

    @Test
    void testSetToMax() {
        final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 10, (byte) 0, (byte) 5);

        assertEquals((byte) 5, testRangeAttributeByte.get().byteValue());

        testRangeAttributeByte.setToMax();

        assertEquals((byte) 10, testRangeAttributeByte.get().byteValue());
    }

    @Test
    void testGetMin() {
        final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 10, (byte) 1, (byte) 5);
        final RangeAttribute<Short> testRangeAttributeShort = new RangeAttribute<>((short) 10, (short) 1, (short) 5);
        final RangeAttribute<Integer> testRangeAttributeInt = new RangeAttribute<>(10, 1, 5);
        final RangeAttribute<Long> testRangeAttributeLong = new RangeAttribute<>(10L, 1L, 5L);
        final RangeAttribute<Float> testRangeAttributeFloat = new RangeAttribute<>(10.0f, 1.5f, 5.0f);
        final RangeAttribute<Double> testRangeAttributeDouble = new RangeAttribute<>(10.0, 1.5, 5.0);

        testRangeAttributeByte.addMinModifier(new AttributeModifier<Byte>(Modification.MULTIPLY, 2));
        testRangeAttributeShort.addMinModifier(new AttributeModifier<Short>(Modification.MULTIPLY, 2));
        testRangeAttributeInt.addMinModifier(new AttributeModifier<Integer>(Modification.MULTIPLY, 2));
        testRangeAttributeLong.addMinModifier(new AttributeModifier<Long>(Modification.MULTIPLY, 2));
        testRangeAttributeFloat.addMinModifier(new AttributeModifier<Float>(Modification.MULTIPLY, 2));
        testRangeAttributeDouble.addMinModifier(new AttributeModifier<Double>(Modification.MULTIPLY, 2));

        assertEquals((byte) 2, testRangeAttributeByte.getMin().byteValue());
        assertEquals((short) 2, testRangeAttributeShort.getMin().shortValue());
        assertEquals(2, testRangeAttributeInt.getMin().intValue());
        assertEquals(2L, testRangeAttributeLong.getMin().longValue());
        assertEquals(3.0f, testRangeAttributeFloat.getMin().floatValue());
        assertEquals(3.0, testRangeAttributeDouble.getMin().doubleValue());
    }

    @Test
    void testGetMax() {
        final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 10, (byte) 1, (byte) 5);
        final RangeAttribute<Short> testRangeAttributeShort = new RangeAttribute<>((short) 10, (short) 1, (short) 5);
        final RangeAttribute<Integer> testRangeAttributeInt = new RangeAttribute<>(10, 1, 5);
        final RangeAttribute<Long> testRangeAttributeLong = new RangeAttribute<>(10L, 1L, 5L);
        final RangeAttribute<Float> testRangeAttributeFloat = new RangeAttribute<>(10.0f, 1.5f, 5.0f);
        final RangeAttribute<Double> testRangeAttributeDouble = new RangeAttribute<>(10.0, 1.5, 5.0);

        testRangeAttributeByte.addMaxModifier(new AttributeModifier<Byte>(Modification.MULTIPLY, 2));
        testRangeAttributeShort.addMaxModifier(new AttributeModifier<Short>(Modification.MULTIPLY, 2));
        testRangeAttributeInt.addMaxModifier(new AttributeModifier<Integer>(Modification.MULTIPLY, 2));
        testRangeAttributeLong.addMaxModifier(new AttributeModifier<Long>(Modification.MULTIPLY, 2));
        testRangeAttributeFloat.addMaxModifier(new AttributeModifier<Float>(Modification.MULTIPLY, 2));
        testRangeAttributeDouble.addMaxModifier(new AttributeModifier<Double>(Modification.MULTIPLY, 2));

        assertEquals((byte) 20, testRangeAttributeByte.getMax().byteValue());
        assertEquals((short) 20, testRangeAttributeShort.getMax().shortValue());
        assertEquals(20, testRangeAttributeInt.getMax().intValue());
        assertEquals(20L, testRangeAttributeLong.getMax().longValue());
        assertEquals(20.0f, testRangeAttributeFloat.getMax().floatValue());
        assertEquals(20.0, testRangeAttributeDouble.getMax().doubleValue());
    }

    @Test
    void testGetWithValueOutOfRangeMax() {
        final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 10, (byte) 1, (byte) 5);

        testRangeAttributeByte.addModifier(new AttributeModifier<>(Modification.MULTIPLY, 3));

        assertEquals((byte) 10, testRangeAttributeByte.get().byteValue());

        testRangeAttributeByte.addMaxModifier(new AttributeModifier<>(Modification.ADD, 10));

        assertEquals((byte) 15, testRangeAttributeByte.get().byteValue());
    }

    @Test
    void testGetWithValueOutOfRangeMin() {
        final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 10, (byte) 1, (byte) 5);

        testRangeAttributeByte.addModifier(new AttributeModifier<>(Modification.SUBSTRACT, 15));

        assertEquals((byte) 1, testRangeAttributeByte.get().byteValue());
    }

    @Test
    void testGetRelativeCurrentValue() {
        final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 10, (byte) 1, (byte) 5);
        final RangeAttribute<Short> testRangeAttributeShort = new RangeAttribute<>((short) 10, (short) 1, (short) 5);
        final RangeAttribute<Integer> testRangeAttributeInt = new RangeAttribute<>(10, 1, 5);
        final RangeAttribute<Long> testRangeAttributeLong = new RangeAttribute<>(10L, 1L, 5L);
        final RangeAttribute<Float> testRangeAttributeFloat = new RangeAttribute<>(10.0f, 1.0f, 5.0f);
        final RangeAttribute<Double> testRangeAttributeDouble = new RangeAttribute<>(10.0, 1.0, 5.0);

        assertEquals(0.5, testRangeAttributeByte.getRelativeCurrentValue());
        assertEquals(0.5, testRangeAttributeShort.getRelativeCurrentValue());
        assertEquals(0.5, testRangeAttributeInt.getRelativeCurrentValue());
        assertEquals(0.5, testRangeAttributeLong.getRelativeCurrentValue());
        assertEquals(0.5, testRangeAttributeFloat.getRelativeCurrentValue());
        assertEquals(0.5, testRangeAttributeDouble.getRelativeCurrentValue());
    }

    @Test
    void testSetMaxBaseValue() {
        final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 10, (byte) 1, (byte) 5);

        assertEquals((byte) 10, testRangeAttributeByte.getMax().byteValue());

        testRangeAttributeByte.setMaxBaseValue((byte) 20);
        assertEquals((byte) 20, testRangeAttributeByte.getMax().byteValue());
    }

    @Test
    void testSetMinBaseValue() {
        final RangeAttribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 50, (byte) 10, (byte) 5);

        assertEquals((byte) 10, testRangeAttributeByte.getMin().byteValue());

        testRangeAttributeByte.setMinBaseValue((byte) 5);
        assertEquals((byte) 5, testRangeAttributeByte.getMin().byteValue());
    }

    @Test
    public void testModifyBaseValue() {
        final Attribute<Byte> testRangeAttributeByte = new RangeAttribute<>((byte) 100, (byte) 0, (byte) 10);
        final Attribute<Short> testRangeAttributeShort = new RangeAttribute<>((short) 200, (short) 0, (short) 10);
        final Attribute<Integer> testRangeAttributeInt = new RangeAttribute<>(200, 0, 10);
        final Attribute<Long> testRangeAttributeLong = new RangeAttribute<>(2000L, 0L, 10L);
        final Attribute<Float> testRangeAttributeFloat = new RangeAttribute<>(200.0f, 0.0f, 10.0f);
        final Attribute<Double> testRangeAttributeDouble = new RangeAttribute<>(200.0, 0.0, 10.0);

        testRangeAttributeByte.modifyBaseValue(new AttributeModifier<Byte>(Modification.ADD, (byte) 10));
        testRangeAttributeShort.modifyBaseValue(new AttributeModifier<Short>(Modification.ADD, (short) 100));
        testRangeAttributeInt.modifyBaseValue(new AttributeModifier<Integer>(Modification.ADD, 100));
        testRangeAttributeLong.modifyBaseValue(new AttributeModifier<Long>(Modification.ADD, 1000L));
        testRangeAttributeFloat.modifyBaseValue(new AttributeModifier<Float>(Modification.ADD, 101.1f));
        testRangeAttributeDouble.modifyBaseValue(new AttributeModifier<Double>(Modification.ADD, 101.1));

        assertEquals((byte) 20, testRangeAttributeByte.get().byteValue());
        assertEquals((short) 110, testRangeAttributeShort.get().byteValue());
        assertEquals(110, testRangeAttributeInt.get().intValue());
        assertEquals(1010L, testRangeAttributeLong.get().longValue());
        assertEquals(111.1f, testRangeAttributeFloat.get().floatValue(), 0.0001f);
        assertEquals(111.1, testRangeAttributeDouble.get().doubleValue(), 0.0000001);
    }
}