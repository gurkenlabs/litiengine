package de.gurkenlabs.litiengine.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArrayUtilitiesTests {

  @BeforeEach
  public void setup() {
    Logger.getLogger(ArrayUtilities.class.getName()).setUseParentHandlers(false);
  }

  @Test
  public void testByteArrayConcat() {
    byte[] arr1 = new byte[] { 1, 2, 3, 4, 5 };
    byte[] arr2 = new byte[] { 6, 7, 8, 9 };

    byte[] arr3 = ArrayUtilities.concat(arr1, arr2);

    assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, arr3);
  }

  @Test
  public void testIntegerArrayFromCommaSeparatedString() {
    String testStringWithInts = "100,200,300,1,2,3";
    String testStringWithoutInts = "paslikodja,2asdasd,sadasd";
    String testNull = null;
    String testEmpty = "";

    int[] intsFromString = ArrayUtilities.splitInt(testStringWithInts);
    int[] stringWithoutInts = ArrayUtilities.splitInt(testStringWithoutInts);
    int[] nullString = ArrayUtilities.splitInt(testNull);
    int[] emptyString = ArrayUtilities.splitInt(testEmpty);

    assertArrayEquals(new int[] { 100, 200, 300, 1, 2, 3 }, intsFromString);
    assertArrayEquals(new int[] { 0, 0, 0 }, stringWithoutInts);
    assertArrayEquals(new int[] {}, nullString);
    assertArrayEquals(new int[] {}, emptyString);
  }

  @Test
  public void testDoubleArrayFromCommaSeparatedString() {
    String testStringWithDoubles = "100.1,200.2,300.3,1.4,2.5,3.6";
    String testStringWithoutDoubles = "paslikodja,2asdasd,sadasd";
    String testNull = null;
    String testEmpty = "";

    double[] doublesFromString = ArrayUtilities.splitDouble(testStringWithDoubles);
    double[] stringWithoutDoubles = ArrayUtilities.splitDouble(testStringWithoutDoubles);
    double[] nullString = ArrayUtilities.splitDouble(testNull);
    double[] emptyString = ArrayUtilities.splitDouble(testEmpty);

    assertArrayEquals(new double[] { 100.1, 200.2, 300.3, 1.4, 2.5, 3.6 }, doublesFromString);
    assertArrayEquals(new double[] { 0, 0, 0 }, stringWithoutDoubles);
    assertArrayEquals(new double[] {}, nullString);
    assertArrayEquals(new double[] {}, emptyString);
  }

  @Test
  public void testCommaSeparatedStringFromIntegerArray() {
    int[] intsArr = new int[] { 100, 200, 300, 1, 2, 3 };

    String testStringWithInts = ArrayUtilities.join(intsArr);

    String testEmpty = ArrayUtilities.join(new int[] {});

    assertEquals("100,200,300,1,2,3", testStringWithInts);
    assertEquals("", testEmpty);
  }

  @ParameterizedTest
  @MethodSource("getArray")
  public void testTwoDimensionalArrayToList(List<Integer> expectedValue) {
    Integer[][] arr = new Integer[][] {
            { 0, 0, 0, },
            { 1, 1, 1, },
            { 2, 2, 2, },
    };

    List<Integer> list = ArrayUtilities.toList(arr);
    for (int i = 0; i < list.size(); i++) {
      assertEquals(expectedValue.get(i).intValue(), list.get(i).intValue());
    }
  }

  private static Stream<Arguments> getArray() {
    return Stream.of(
            Arguments.of(Arrays.asList(0, 0, 0, 1, 1, 1, 2, 2, 2))
    );
  }

  @Test
  public void testAppend() {
    Integer[] test = new Integer[] { 1, 2, 3, 4, 5 };
    Integer[] result = ArrayUtilities.append(test, 6);

    assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5, 6 }, result);
  }

  @Test
  public void testDistinct() {
    Integer[] first = new Integer[] { 1, 2, 3, 4, 5 };
    Integer[] second = new Integer[] { 1, 2, 3, 4, 5, 6 };
    Integer[] result = ArrayUtilities.distinct(first, second);

    assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5, 6 }, result);
  }

  @Test
  public void testContains() {
    Object [] first = new Object[] { 1, 2, 3, 4, 5, null };
    Object [] second = new Object[] {};

    assertTrue(ArrayUtilities.contains(first, 2));
    assertTrue(ArrayUtilities.contains(first, null));
    assertFalse(ArrayUtilities.contains(second, ""));
  }

  @Test
  public void testRemove() {
    Integer[] test = new Integer[] { 1, 2, 3, 4, 5 };
    Integer[] result = ArrayUtilities.remove(test, 6);

    assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5 }, result);
  }

}
