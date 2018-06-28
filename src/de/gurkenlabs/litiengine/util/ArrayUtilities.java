package de.gurkenlabs.litiengine.util;

import java.util.Arrays;
import java.util.List;
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

  public static int[] getIntegerArray(String commaSeperatedString) {
    if (commaSeperatedString == null || commaSeperatedString.isEmpty()) {
      return new int[0];
    }

    final String[] split = commaSeperatedString.split(",");
    int[] integers = new int[split.length];
    if (integers.length == 0) {
      return integers;
    }

    for (int i = 0; i < split.length; i++) {
      if (split[i] == null || split[i].isEmpty()) {
        continue;
      }

      try {
        integers[i] = Integer.parseInt(split[i]);
      } catch (final NumberFormatException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    return integers;
  }

  public static double[] getDoubleArray(String commaSeperatedString) {
    if (commaSeperatedString == null || commaSeperatedString.isEmpty()) {
      return new double[0];
    }

    final String[] split = commaSeperatedString.split(",");
    double[] doubles = new double[split.length];
    if (doubles.length == 0) {
      return doubles;
    }

    for (int i = 0; i < split.length; i++) {
      if (split[i] == null || split[i].isEmpty()) {
        continue;
      }

      try {
        doubles[i] = Double.parseDouble(split[i]);
      } catch (final NumberFormatException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    return doubles;
  }

  public static String getCommaSeparatedString(int[] arr) {
    return getCommaSeparatedString(Arrays.stream(arr).boxed().toArray(Integer[]::new));
  }

  public static String getCommaSeparatedString(double[] arr) {
    return getCommaSeparatedString(Arrays.stream(arr).boxed().toArray(Double[]::new));
  }

  public static <T> String getCommaSeparatedString(List<T> list) {
    if (list == null || list.isEmpty()) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
      sb.append(list.get(i));
      if (i < list.size() - 1) {
        sb.append(',');
      }
    }

    return sb.toString();
  }

  public static <T> String getCommaSeparatedString(T[] arr) {
    return getCommaSeparatedString(Arrays.asList(arr));
  }

  public static <T> T getRandom(T[] arr) {
    if (arr.length == 0) {
      return null;
    }

    final int randomIndex = new Random().nextInt(arr.length);
    return arr[randomIndex];
  }

  public static boolean containsArgument(String[] args, String argument) {
    if (args == null || args.length == 0) {
      return false;
    }

    for (int i = 0; i < args.length; i++) {
      final String a = args[i];
      if (a != null && !a.isEmpty() && a.equalsIgnoreCase(argument)) {
        return true;
      }
    }

    return false;
  }

  public static int[] toIntegerArray(List<Integer> intList) {
    Object[] objArray = intList.toArray();
    int[] intArray = new int[objArray.length];
    for (int i = 0; i < intArray.length; i++) {
      intArray[i] = (int) objArray[i];
    }
    return intArray;
  }

}
