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
  public void testIntArrayConcat() {
    int[] arr1 = new int[] { 1, 2, 3, 4, 5 };
    int[] arr2 = new int[] { 6, 7, 8, 9 };

    int[] arr3 = ArrayUtilities.concat(arr1, arr2);

    assertArrayEquals(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, arr3);
  }

  @Test
  public void testLongArrayConcat() {
    long[] arr1 = new long[] { 1, 2, 3, 4, 5 };
    long[] arr2 = new long[] { 6, 7, 8, 9 };

    long[] arr3 = ArrayUtilities.concat(arr1, arr2);

    assertArrayEquals(new long[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, arr3);
  }

  @Test
  public void testDoubleArrayConcat() {
    double[] arr1 = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0 };
    double[] arr2 = new double[] { 6.0, 7.0, 8.0, 9.0 };

    double[] arr3 = ArrayUtilities.concat(arr1, arr2);

    assertArrayEquals(new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0 }, arr3);
  }

  @Test
  public void testTArrayConcat() {
    Integer[] arr1 = {1, 2, 3, 4, 5};
    Integer[] arr2 = {6, 7, 8, 9};

    Integer[] arr3 = ArrayUtilities.concat(arr1, arr2);

    assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, arr3);
  }

  @Test
  public void testIntegerArrayFromCommaSeparatedString() {
    String testStringWithInts = "100,200,300,1,2,3";
    String testStringWithoutInts = "paslikodja,2asdasd,sadasd";
    String testNull = null;
    String testEmpty = "";
    String testDelimiterEmpty = ",";

    int[] intsFromString = ArrayUtilities.splitInt(testStringWithInts);
    int[] stringWithoutInts = ArrayUtilities.splitInt(testStringWithoutInts);
    int[] nullString = ArrayUtilities.splitInt(testNull);
    int[] emptyString = ArrayUtilities.splitInt(testEmpty);
    int[] emptyDelimiterString = ArrayUtilities.splitInt(testDelimiterEmpty);

    assertArrayEquals(new int[] { 100, 200, 300, 1, 2, 3 }, intsFromString);
    assertArrayEquals(new int[] { 0, 0, 0 }, stringWithoutInts);
    assertArrayEquals(new int[] {}, nullString);
    assertArrayEquals(new int[] {}, emptyString);
    assertArrayEquals(new int[] {,}, emptyDelimiterString);
  }

  @Test
  public void testDoubleArrayFromCommaSeparatedString() {
    String testStringWithDoubles = "100.1,200.2,300.3,1.4,2.5,3.6";
    String testStringWithoutDoubles = "paslikodja,2asdasd,sadasd";
    String testNull = null;
    String testEmpty = "";
    String testDelimiterEmpty = ",";

    double[] doublesFromString = ArrayUtilities.splitDouble(testStringWithDoubles);
    double[] stringWithoutDoubles = ArrayUtilities.splitDouble(testStringWithoutDoubles);
    double[] nullString = ArrayUtilities.splitDouble(testNull);
    double[] emptyString = ArrayUtilities.splitDouble(testEmpty);
    double[] emptyDelimiterString = ArrayUtilities.splitDouble(testDelimiterEmpty);

    assertArrayEquals(new double[] { 100.1, 200.2, 300.3, 1.4, 2.5, 3.6 }, doublesFromString);
    assertArrayEquals(new double[] { 0, 0, 0 }, stringWithoutDoubles);
    assertArrayEquals(new double[] {}, nullString);
    assertArrayEquals(new double[] {}, emptyString);
    assertArrayEquals(new double[] {}, emptyDelimiterString);
  }

  @Test
  public void testCommaSeparatedStringFromIntegerArray() {
    int[] intsArr = new int[] { 100, 200, 300, 1, 2, 3 };
    String testStringWithInts = ArrayUtilities.join(intsArr);
    assertEquals("100,200,300,1,2,3", testStringWithInts);
  }

  @Test
  public void testCommaSeparatedStringFromIntegerArrayEmpty() {
    String testEmpty = ArrayUtilities.join(new int[] {});
    assertEquals("", testEmpty);
  }

  @Test
  public void testCommaSeparatedStringFromIntegerArrayDelimiter() {
    int[] intsArr = new int[] { 100, 200, 300, 1, 2, 3 };
    String testStringWithIntsDelimiter = ArrayUtilities.join(intsArr, ";");
    assertEquals("100;200;300;1;2;3", testStringWithIntsDelimiter);
  }

  @Test
  public void testCommaSeparatedStringFromBooleanArray() {
    boolean[] boolArr = new boolean[] {true, false, true};
    String testStringWithBool = ArrayUtilities.join(boolArr);
    assertEquals("true,false,true", testStringWithBool);
  }

  @Test
  public void testCommaSeparatedStringFromBooleanEmpty() {
    String testEmpty = ArrayUtilities.join(new boolean[] {});
    assertEquals("", testEmpty);
  }

  @Test
  public void testCommaSeparatedStringFromBooleanArrayDelimiter() {
    boolean[] boolArr = new boolean[] {true, false, true};
    String testStringWithBoolDelimiter = ArrayUtilities.join(boolArr, ";");
    assertEquals("true;false;true", testStringWithBoolDelimiter);
  }

  @Test
  public void testCommaSeparatedStringFromDoubleArray() {
    double[] doubleArr = new double[] {100.0, 200.0, 300.0, 1.0, 2.0, 3.0};
    String testStringWithDouble = ArrayUtilities.join(doubleArr);
    assertEquals("100.0,200.0,300.0,1.0,2.0,3.0", testStringWithDouble);
  }

  @Test
  public void testCommaSeparatedStringFromDoubleArrayEmpty() {
    String testEmpty = ArrayUtilities.join(new double[] {});
    assertEquals("", testEmpty);
  }

  @Test
  public void testCommaSeparatedStringFromDoubleArrayDelimiter() {
    double[] doubleArr = new double[] {100.0, 200.0, 300.0, 1.0, 2.0, 3.0};
    String testStringWithDoubleDelimiter = ArrayUtilities.join(doubleArr, ";");
    assertEquals("100.0;200.0;300.0;1.0;2.0;3.0", testStringWithDoubleDelimiter);
  }

  @Test
  public void testCommaSeparatedStringFromFloatArray() {
    float[] floatArr = new float[] {100.0f, 200.0f, 300.0f, 1.0f, 2.0f, 3.0f};
    String testStringWithFloat = ArrayUtilities.join(floatArr);
    assertEquals("100.0,200.0,300.0,1.0,2.0,3.0", testStringWithFloat);
  }

  @Test
  public void testCommaSeparatedStringFromFloatArrayEmpty() {
    String testEmpty = ArrayUtilities.join(new double[] {});
    assertEquals("", testEmpty);
  }

  @Test
  public void testCommaSeparatedStringFromFloatArrayDelimiter() {
    float[] floatArr = new float[] {100.0f, 200.0f, 300.0f, 1.0f, 2.0f, 3.0f};
    String testStringWithFloatDelimiter = ArrayUtilities.join(floatArr, ";");
    assertEquals("100.0;200.0;300.0;1.0;2.0;3.0", testStringWithFloatDelimiter);
  }

  @Test
  public void testCommaSeparatedStringFromStringArray() {
    String[] stringArr = new String[] {"test", "test2", "test3"};
    String testStringWithString = ArrayUtilities.join(stringArr);
    assertEquals("test,test2,test3", testStringWithString);
  }

  @Test
  public void testCommaSeparatedStringFromStringArrayEmpty() {
    String testEmpty = ArrayUtilities.join(new String[] {});
    assertEquals("", testEmpty);
  }

  @Test
  public void testCommaSeparatedStringFromStringArrayDelimiter() {
    String[] stringArr = new String[] {"test", "test2", "test3"};
    String testStringWithStringDelimiter = ArrayUtilities.join(stringArr, ";");
    assertEquals("test;test2;test3", testStringWithStringDelimiter);
  }

  @Test
  public void testCommaSeparatedStringFromShortArray() {
    short[] shortArr = new short[] {100, 200, 300, 1, 2, 3};
    String testStringWithShort = ArrayUtilities.join(shortArr);
    assertEquals("100,200,300,1,2,3", testStringWithShort);
  }

  @Test
  public void testCommaSeparatedStringFromShortArrayEmpty() {
    String testEmpty = ArrayUtilities.join(new short[] {});
    assertEquals("", testEmpty);
  }

  @Test
  public void testCommaSeparatedStringFromShortArrayDelimiter() {
    short[] shortArr = new short[] {100, 200, 300, 1, 2, 3};
    String testStringWithShortDelimiter = ArrayUtilities.join(shortArr, ";");
    assertEquals("100;200;300;1;2;3", testStringWithShortDelimiter);
  }


  @Test
  public void testCommaSeparatedStringFromLongArray() {
    long[] longArr = new long[] {100, 200, 300, 1, 2, 3};
    String testStringWithLong = ArrayUtilities.join(longArr);
    assertEquals("100,200,300,1,2,3", testStringWithLong);
  }

  @Test
  public void testCommaSeparatedStringFromLongArrayEmpty() {
    long[] longArr = new long[] {100, 200, 300, 1, 2, 3};
    String testStringWithLongDelimiter = ArrayUtilities.join(longArr, ";");
    assertEquals("100;200;300;1;2;3", testStringWithLongDelimiter);
  }

  @Test
  public void testCommaSeparatedStringFromLongArrayDelimiter() {
    long[] longArr = new long[] {100, 200, 300, 1, 2, 3};
    String testStringWithLongDelimiter = ArrayUtilities.join(longArr, ";");
    assertEquals("100;200;300;1;2;3", testStringWithLongDelimiter);
  }

  @Test
  public void testCommaSeparatedStringFromByteArray() {
    byte[] byteArr = new byte[] {100, 127, -128, 1, 2, 3};
    String testStringWithByte = ArrayUtilities.join(byteArr);
    assertEquals("100,127,-128,1,2,3", testStringWithByte);
  }

  @Test
  public void testCommaSeparatedStringFromByteArrayEmpty() {
    String testEmpty = ArrayUtilities.join(new byte[] {});
    assertEquals("", testEmpty);
  }

  @Test
  public void testCommaSeparatedStringFromByteArrayDelimiter() {
    byte[] byteArr = new byte[] {100, 127, -128, 1, 2, 3};
    String testStringWithByteDelimiter = ArrayUtilities.join(byteArr, ";");
    assertEquals("100;127;-128;1;2;3", testStringWithByteDelimiter);
  }

  @Test
  public void testCommaSeparatedStringFromCollectionArray() {
    List<String> collectionArr = Arrays.asList("test", "test2", "test3");
    String testStringWithCollectionDelimiter = ArrayUtilities.join(collectionArr, ";");
    assertEquals("test;test2;test3", testStringWithCollectionDelimiter);
  }

  @Test
  public void testCommaSeparatedStringFromCollectionArrayEmpty() {
    String testEmpty = ArrayUtilities.join(new String[] {});
    assertEquals("", testEmpty);
  }

  @Test
  public void testCommaSeparatedStringFromCollectionArrayDelimiter() {
    List<String> collectionArr = Arrays.asList("test", "test2", "test3");
    String testStringWithCollectionDelimiter = ArrayUtilities.join(collectionArr, ";");
    assertEquals("test;test2;test3", testStringWithCollectionDelimiter);
  }

  @Test
  public void testTwoDimensionalArrayToList() {
    Integer[][] arr = new Integer[][] {
        { 0, 0, 0, },
        { 1, 1, 1, },
        { 2, 2, 2, },
    };

    List<Integer> list = ArrayUtilities.toList(arr);

    assertEquals(0, list.get(0).intValue());
    assertEquals(0, list.get(1).intValue());
    assertEquals(0, list.get(2).intValue());

    assertEquals(1, list.get(3).intValue());
    assertEquals(1, list.get(4).intValue());
    assertEquals(1, list.get(5).intValue());

    assertEquals(2, list.get(6).intValue());
    assertEquals(2, list.get(7).intValue());
    assertEquals(2, list.get(8).intValue());
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

  @ParameterizedTest
  @MethodSource("getContains")
  public void testContains(Object[] array, Object value, Boolean expected) {
    assertEquals(expected, ArrayUtilities.contains(array, value));
  }

  private static Stream<Arguments> getContains() {
    return Stream.of(
            Arguments.of(new Object[] { 1, 2, 3, 4, 5, null }, 2, true),
            Arguments.of(new Object[] { 1, 2, 3, 4, 5, null }, null, true),
            Arguments.of(new Object[] {}, "", false),
            Arguments.of(new Object[] {null}, null, true),
            Arguments.of(new Object[] {4}, 4, true)
    );
  }

  @ParameterizedTest
  @MethodSource("getContainsString")
  public void testContainsString(String[] string, String argument, Boolean ignoreCase, Boolean expected) {
    assertEquals(expected, ArrayUtilities.contains(string, argument, ignoreCase));
  }

  private static Stream<Arguments> getContainsString() {
    return Stream.of(
            Arguments.of(new String[] {"test", "test123"}, "Test", true, true),
            Arguments.of(new String[] {"test", "test123"}, "Test", false, false),
            Arguments.of(new String[] {"test", "test123", null, ""}, "Test", false, false),
            Arguments.of(new String[] {"test", "test123", null, ""}, null, false, false),
            Arguments.of(new String[] {}, "", true, false),
            Arguments.of(new String[] {}, "", true, false),
            Arguments.of(new String[] {null}, null, false, false),
            Arguments.of(new String[] {null}, null, true, false),
            Arguments.of(new String[] {"test"}, "Test", true, true),
            Arguments.of(new String[] {"test"}, "test", false, true),
            Arguments.of(null, null, false, false)

    );
  }

  @Test
  public void testRemove() {
    Integer[] test = new Integer[] { 1, 2, 3, 4, 5 };
    Integer[] result = ArrayUtilities.remove(test, 6);

    assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5 }, result);
  }

}
