package com.litiengine.util.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

public class CompressionUtilitiesTests {

  @Test
  public void testCompression() {
    byte[] arr = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

    byte[] compressed = CompressionUtilities.compress(arr);
    byte[] decompressed = CompressionUtilities.decompress(compressed);
    assertArrayEquals(arr, decompressed);
  }
}
