package de.gurkenlabs.litiengine.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArrayUtilitiesTests {

  @BeforeEach
  public void setup() {
    Logger.getLogger(ArrayUtilities.class.getName()).setUseParentHandlers(false);
  }

  @Test
  public void testByteArrayConcat() {
    byte[] arr1 = new byte[] { 1, 2, 3, 4, 5 };
    byte[] arr2 = new byte[] { 6, 7, 8, 9 };

    byte[] arr3 = ArrayUtilities.arrayConcat(arr1, arr2);

    assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, arr3);
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

    assertArrayEquals(new int[] { 100, 200, 300, 1, 2, 3 }, intsFromString);
    assertArrayEquals(new int[] { 0, 0, 0 }, stringWithoutInts);
    assertArrayEquals(new int[] {}, nullString);
    assertArrayEquals(new int[] {}, emptyString);
  }

  @Test
  public void testCommaSeparatedStringFromIntegerArray() {
    int[] intsArr = new int[] { 100, 200, 300, 1, 2, 3 };

    String testStringWithInts = ArrayUtilities.join(intsArr);

    String testEmpty = ArrayUtilities.join(new int[] {});

    assertEquals("100,200,300,1,2,3", testStringWithInts);
    assertNull(testEmpty);
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
}
