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
}
