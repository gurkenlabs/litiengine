package de.gurkenlabs.util;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ArrayUtilities {
  private static final Logger log = Logger.getLogger(ArrayUtilities.class.getName());

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

  public static int[] getIntegerArray(String commaSeparatedIntegers) {
    if (commaSeparatedIntegers == null || commaSeparatedIntegers.isEmpty()) {
      return new int[0];
    }

    final String[] split = commaSeparatedIntegers.split(",");
    int[] integers = new int[split.length];
    if (integers.length == 0) {
      return integers;
    }

    for (int i = 0; i < split.length; i++) {
      String integerString = split[i];
      if (integerString == null || integerString.isEmpty()) {
        continue;
      }

      try {
        integers[i] = Integer.parseInt(integerString);
      } catch (final NumberFormatException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    return integers;
  }

  public static <T> T getRandom(T[] arr) {
    if (arr.length == 0) {
      return null;
    }

    final int randomIndex = new Random().nextInt(arr.length);
    return arr[randomIndex];
  }
}
