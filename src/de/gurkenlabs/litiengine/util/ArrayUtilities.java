package de.gurkenlabs.litiengine.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ArrayUtilities {
  public static final String DEFALUT_SEPARATOR = ",";
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

  public static String join(boolean[] arr) {
    return join(arr, DEFALUT_SEPARATOR);
  }

  public static String join(boolean[] arr, String separator) {
    List<Boolean> list = new ArrayList<>();
    for (int i = 0; i < arr.length; i++) {
      list.add(arr[i]);
    }

    return join(list, separator);
  }

  public static String join(int[] arr) {
    return join(arr, DEFALUT_SEPARATOR);
  }

  public static String join(int[] arr, String separator) {
    return join(Arrays.stream(arr).boxed().toArray(Integer[]::new), separator);
  }

  public static String join(double[] arr) {
    return join(arr, DEFALUT_SEPARATOR);
  }

  public static String join(double[] arr, String separator) {
    return join(Arrays.stream(arr).boxed().toArray(Double[]::new), separator);
  }

  public static String join(float[] arr) {
    return join(arr, DEFALUT_SEPARATOR);
  }

  public static String join(float[] arr, String separator) {
    List<Float> list = new ArrayList<>();
    for (int i = 0; i < arr.length; i++) {
      list.add(arr[i]);
    }
    return join(list, separator);
  }

  public static String join(short[] arr) {
    return join(arr, DEFALUT_SEPARATOR);
  }

  public static String join(short[] arr, String separator) {
    List<Short> list = new ArrayList<>();
    for (int i = 0; i < arr.length; i++) {
      list.add(arr[i]);
    }
    return join(list, separator);
  }

  public static String join(long[] arr) {
    return join(arr, DEFALUT_SEPARATOR);
  }

  public static String join(long[] arr, String separator) {
    return join(Arrays.stream(arr).boxed().toArray(Long[]::new), separator);
  }

  public static String join(byte[] arr) {
    return join(arr, DEFALUT_SEPARATOR);
  }

  public static String join(byte[] arr, String separator) {
    List<Byte> list = new ArrayList<>();
    for (int i = 0; i < arr.length; i++) {
      list.add(arr[i]);
    }
    return join(list, separator);
  }

  public static <T> String join(List<T> list) {
    return join(list, ",");
  }

  public static <T> String join(List<T> list, String separator) {
    if (list == null || list.isEmpty()) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
      sb.append(list.get(i));
      if (i < list.size() - 1) {
        sb.append(separator);
      }
    }

    return sb.toString();
  }

  public static <T> String join(T[] arr) {
    return join(Arrays.asList(arr), DEFALUT_SEPARATOR);
  }

  public static <T> String join(T[] arr, String separator) {
    return join(Arrays.asList(arr), separator);
  }

  public static <T> List<T> toList(T[][] arr) {
    List<T> list = new ArrayList<>();
    for (T[] rows : arr) {
      list.addAll(Arrays.asList(rows));
    }

    return list;
  }

  public static <T> T getRandom(T[] arr) {
    if (arr.length == 0) {
      return null;
    }

    final int randomIndex = new Random().nextInt(arr.length);
    return arr[randomIndex];
  }

  public static <T> boolean contains(T[] arr, T value) {
    if (value == null) {
      return false;
    }

    for (T v : arr) {
      if (v != null && v.equals(value)) {
        return true;
      }
    }

    return false;
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

  /**
   * Removes the specified deleteItem from the input array and returns a trimmed new array instance without null entries.
   * The resulting array will have a length -1;
   * 
   * @param <T>
   *          The element type of the array.
   * @param input
   *          The original array
   * @param deleteItem
   *          The item to delete
   * @return A new array with the length input.length - 1.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] remove(T[] input, T deleteItem) {
    List<T> result = new ArrayList<>();

    for (T item : input) {
      if (!deleteItem.equals(item)) {
        result.add(item);
      }
    }

    result.removeAll(Collections.singleton(null));
    return result.toArray((T[]) Array.newInstance(input.getClass().getComponentType(), result.size()));
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
