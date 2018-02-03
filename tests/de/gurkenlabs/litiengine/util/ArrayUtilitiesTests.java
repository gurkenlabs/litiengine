package de.gurkenlabs.litiengine.util;

import org.junit.Assert;
import org.junit.Test;

import de.gurkenlabs.util.ArrayUtilities;

public class ArrayUtilitiesTests {

  @Test
  public void testByteArrayConcat() {
    byte[] arr1 = new byte[] { 1, 2, 3, 4, 5 };
    byte[] arr2 = new byte[] { 6, 7, 8, 9 };

    byte[] arr3 = ArrayUtilities.arrayConcat(arr1, arr2);

    Assert.assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, arr3);
  }

  @Test
  public void testIntegerArrayFromCommaSeparatedString() {
    String testStringWithInts = "100,200,300,1,2,3";
    String testStringWithoutInts = "paslikodja,2asdasd,sadasd";
    String testNull = null;
    String testEmpty = "";

    int[] intsFromString = ArrayUtilities.getIntegerArray(testStringWithInts);
    int[] stringWithoutInts = ArrayUtilities.getIntegerArray(testStringWithoutInts);
    int[] nullString = ArrayUtilities.getIntegerArray(testNull);
    int[] emptyString = ArrayUtilities.getIntegerArray(testEmpty);

    Assert.assertArrayEquals(new int[] { 100, 200, 300, 1, 2, 3 }, intsFromString);
    Assert.assertArrayEquals(new int[] { 0, 0, 0 }, stringWithoutInts);
    Assert.assertArrayEquals(new int[] {}, nullString);
    Assert.assertArrayEquals(new int[] {}, emptyString);
  }

  @Test
  public void testCommaSeparatedStringFromIntegerArray() {
    int[] intsArr = new int[] { 100, 200, 300, 1, 2, 3 };

    String testStringWithInts = ArrayUtilities.getCommaSeparatedString(intsArr);

    String testEmpty = ArrayUtilities.getCommaSeparatedString(new int[] {});

    Assert.assertEquals("100,200,300,1,2,3", testStringWithInts);
    Assert.assertNull(testEmpty);
  }
}
