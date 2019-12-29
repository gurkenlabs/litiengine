package de.gurkenlabs.litiengine.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ArrayUtilities {
  private static final Logger log = Logger.getLogger(ArrayUtilities.class.getName());

  private ArrayUtilities() {
    throw new UnsupportedOperationException();
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
    return joinArray(arr, ",");
  }

  public static String join(boolean[] arr, String separator) {
    return joinArray(arr, separator);
  }

  public static String join(int[] arr) {
    return joinArray(arr, ",");
  }

  public static String join(int[] arr, String separator) {
    return joinArray(arr, separator);
  }

  public static String join(double[] arr) {
    return joinArray(arr, ",");
  }

  public static String join(double[] arr, String separator) {
    return joinArray(arr, separator);
  }

  public static String join(float[] arr) {
    return joinArray(arr, ",");
  }

  public static String join(float[] arr, String separator) {
    return joinArray(arr, separator);
  }

  public static String join(short[] arr) {
    return joinArray(arr, ",");
  }

  public static String join(short[] arr, String separator) {
    return joinArray(arr, separator);
  }

  public static String join(long[] arr) {
    return join(arr, ",");
  }

  public static String join(long[] arr, String separator) {
    return joinArray(arr, separator);
  }

  public static String join(byte[] arr) {
    return join(arr, ",");
  }

  public static String join(byte[] arr, String separator) {
    return joinArray(arr, separator);
  }

  public static String join(List<?> list) {
    return joinArray(list.toArray(), ",");
  }

  public static String join(List<?> list, String separator) {
    return joinArray(list.toArray(), separator);
  }

  public static String join(Object[] arr) {
    return joinArray(arr, ",");
  }

  public static String join(Object[] arr, String separator) {
    return joinArray(arr, separator);
  }

  // general method for joining an array
  // encapsulated for type safety
  private static String joinArray(Object arr, String separator) {
    if (arr == null) {
      return null;
    }

    int len = Array.getLength(arr);
    if (len == 0) {
      return null;
    }

    StringBuilder sb = new StringBuilder(String.valueOf(Array.get(arr, 0)));
    for (int i = 1; i < len; i++) {
      sb.append(separator);
      sb.append(Array.get(arr, i));
    }

    return sb.toString();
  }

  public static <T> List<T> toList(T[][] arr) {
    List<T> list = new ArrayList<>();
    for (T[] rows : arr) {
      list.addAll(Arrays.asList(rows));
    }

    return list;
  }

  public static <T> T getRandom(T[] arr) {
    return getRandom(arr, ThreadLocalRandom.current());
  }

  public static <T> T getRandom(T[] arr, Random rand) {
    if (arr.length == 0) {
      return null;
    }
    return arr[rand.nextInt(arr.length)];
  }

  public static void shuffle(Object[] arr) {
    shuffle(arr, ThreadLocalRandom.current());
  }

  public static void shuffle(Object[] arr, Random rand) {
    for (int i = arr.length - 1; i > 0; i--) {
      int swap = rand.nextInt(i + 1);
      Object temp = arr[i];
      arr[i] = arr[swap];
      arr[swap] = temp;
    }
  }

  public static boolean contains(Object[] arr, Object value) {
    if (value == null) {
      return false;
    }

    for (Object v : arr) {
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

  /**
   * Adds the specified item to the input array and returns a new array instance with the length of the input array +1.
   * 
   * @param <T>
   *          The element type of the array.
   * @param input
   *          The original array.
   * @param addItem
   *          The item to add.
   * @return A new array with the item to add appended at the end.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] append(T[] input, T addItem) {
    List<T> result = new ArrayList<>(Arrays.asList(input));
    result.add(addItem);

    return result.toArray((T[]) Array.newInstance(input.getClass().getComponentType(), result.size()));
  }

  /**
   * Combines the two specified arrays by only keeping distinct values.
   * 
   * @param <T>
   *          The element type of the array.
   * @param first
   *          The first array.
   * @param second
   *          The second array.
   * @return A new array with every distinct value of the specified arrays.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] distinct(T[] first, T[] second) {
    List<T> firstList = Arrays.asList(first);
    List<T> secondList = Arrays.asList(second);

    HashSet<T> hash = new HashSet<>(firstList);
    hash.addAll(secondList);

    return hash.toArray((T[]) Array.newInstance(first.getClass().getComponentType(), hash.size()));
  }

  public static int[] toIntegerArray(List<Integer> intList) {
    Integer[] objArray = intList.toArray(new Integer[0]);
    int[] intArray = new int[objArray.length];
    for (int i = 0; i < intArray.length; i++) {
      intArray[i] = objArray[i];
    }
    return intArray;
  }
}
