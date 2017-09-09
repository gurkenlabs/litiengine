package de.gurkenlabs.util;

public final class ArrayUtilities {
  private ArrayUtilities() {
  }

  public static byte[] arrayConcat(final byte[] firstArray, final byte[] secondArray) {
    final int aLen = firstArray.length;
    final int bLen = secondArray.length;
    final byte[] combinedArray = new byte[aLen + bLen];
    System.arraycopy(firstArray, 0, combinedArray, 0, aLen);
    System.arraycopy(secondArray, 0, combinedArray, aLen, bLen);
    return combinedArray;
  }
}
