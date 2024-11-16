package de.gurkenlabs.litiengine.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ArrayUtilities {
  public static final String DEFAULT_STRING_DELIMITER = ",";
  private static final Logger log = Logger.getLogger(ArrayUtilities.class.getName());

  private ArrayUtilities() {
    throw new UnsupportedOperationException();
  }

  /**
   * Concatenates the two specified byte arrays to a new array.
   *
   * @param first  The first array.
   * @param second The second array.
   * @return A new array with both specified arrays in sequence.
   */
  public static byte[] concat(byte[] first, byte[] second) {
    byte[] result = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }

  /**
   * Concatenates the two specified int arrays to a new array.
   *
   * @param first  The first array.
   * @param second The second array.
   * @return A new array with both specified arrays in sequence.
   */
  public static int[] concat(int[] first, int[] second) {
    int[] result = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }

  /**
   * Concatenates the two specified long arrays to a new array.
   *
   * @param first  The first array.
   * @param second The second array.
   * @return A new array with both specified arrays in sequence.
   */
  public static long[] concat(long[] first, long[] second) {
    long[] result = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }

  /**
   * Concatenates the two specified double arrays to a new array.
   *
   * @param first  The first array.
   * @param second The second array.
   * @return A new array with both specified arrays in sequence.
   */
  public static double[] concat(double[] first, double[] second) {
    double[] result = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }

  /**
   * Concatenates the two specified double arrays to a new array.
   *
   * @param <T>    The type of the array elements.
   * @param first  The first array.
   * @param second The second array.
   * @return A new array with both specified arrays in sequence.
   */
  public static <T> T[] concat(T[] first, T[] second) {
    T[] result = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }

  /**
   * Splits the specified string by the {@link #DEFAULT_STRING_DELIMITER} into an int array.
   *
   * @param delimiterSeparatedString The string to split.
   * @return An int array with all separated elements of the specified string.
   */
  public static int[] splitInt(String delimiterSeparatedString) {
    return splitInt(delimiterSeparatedString, DEFAULT_STRING_DELIMITER);
  }

  /**
   * Splits the specified string by the defined delimiter into an int array.
   *
   * @param delimiterSeparatedString The string to split.
   * @param delimiter                The delimiter by which to split the elements.
   * @return An int array with all separated elements of the specified string.
   */
  public static int[] splitInt(String delimiterSeparatedString, String delimiter) {
    if (delimiterSeparatedString == null || delimiterSeparatedString.isEmpty()) {
      return new int[0];
    }

    final String[] split = delimiterSeparatedString.split(delimiter);
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

  /**
   * Splits the specified string by the {@link #DEFAULT_STRING_DELIMITER} into a double array.
   *
   * @param delimiterSeparatedString The string to split.
   * @return An double array with all separated elements of the specified string.
   */
  public static double[] splitDouble(String delimiterSeparatedString) {
    return splitDouble(delimiterSeparatedString, DEFAULT_STRING_DELIMITER);
  }

  /**
   * Splits the specified string by the defined delimiter into a double array.
   *
   * @param delimiterSeparatedString The string to split.
   * @param delimiter                The delimiter by which to split the elements.
   * @return An double array with all separated elements of the specified string.
   */
  public static double[] splitDouble(String delimiterSeparatedString, String delimiter) {
    if (delimiterSeparatedString == null || delimiterSeparatedString.isEmpty()) {
      return new double[0];
    }

    final String[] split = delimiterSeparatedString.split(delimiter);
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

  /**
   * Joins the specified array with the {@link #DEFAULT_STRING_DELIMITER}.
   *
   * @param arr The array that provides the elements to be joined.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(boolean[] arr) {
    return joinArray(arr, DEFAULT_STRING_DELIMITER);
  }

  /**
   * Joins the specified array with the defined delimiter.
   *
   * @param arr       The array that provides the elements to be joined.
   * @param delimiter The delimiter used to separate the elements with.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(boolean[] arr, String delimiter) {
    return joinArray(arr, delimiter);
  }

  /**
   * Joins the specified array with the {@link #DEFAULT_STRING_DELIMITER}.
   *
   * @param arr The array that provides the elements to be joined.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(int[] arr) {
    return joinArray(arr, DEFAULT_STRING_DELIMITER);
  }

  /**
   * Joins the specified array with the defined delimiter.
   *
   * @param arr       The array that provides the elements to be joined.
   * @param delimiter The delimiter used to separate the elements with.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(int[] arr, String delimiter) {
    return joinArray(arr, delimiter);
  }

  /**
   * Joins the specified array with the {@link #DEFAULT_STRING_DELIMITER}.
   *
   * @param arr The array that provides the elements to be joined.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(double[] arr) {
    return joinArray(arr, DEFAULT_STRING_DELIMITER);
  }

  /**
   * Joins the specified array with the defined delimiter.
   *
   * @param arr       The array that provides the elements to be joined.
   * @param delimiter The delimiter used to separate the elements with.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(double[] arr, String delimiter) {
    return joinArray(arr, delimiter);
  }

  /**
   * Joins the specified array with the {@link #DEFAULT_STRING_DELIMITER}.
   *
   * @param arr The array that provides the elements to be joined.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(float[] arr) {
    return joinArray(arr, DEFAULT_STRING_DELIMITER);
  }

  /**
   * Joins the specified array with the defined delimiter.
   *
   * @param arr       The array that provides the elements to be joined.
   * @param delimiter The delimiter used to separate the elements with.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(float[] arr, String delimiter) {
    return joinArray(arr, delimiter);
  }

  /**
   * Joins the specified array with the {@link #DEFAULT_STRING_DELIMITER}.
   *
   * @param arr The array that provides the elements to be joined.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(short[] arr) {
    return joinArray(arr, DEFAULT_STRING_DELIMITER);
  }

  public static String join(short[] arr, String delimiter) {
    return joinArray(arr, delimiter);
  }

  /**
   * Joins the specified array with the {@link #DEFAULT_STRING_DELIMITER}.
   *
   * @param arr The array that provides the elements to be joined.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(long[] arr) {
    return join(arr, DEFAULT_STRING_DELIMITER);
  }

  /**
   * Joins the specified array with the defined delimiter.
   *
   * @param arr       The array that provides the elements to be joined.
   * @param delimiter The delimiter used to separate the elements with.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(long[] arr, String delimiter) {
    return joinArray(arr, delimiter);
  }

  /**
   * Joins the specified array with the {@link #DEFAULT_STRING_DELIMITER}.
   *
   * @param arr The array that provides the elements to be joined.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(byte[] arr) {
    return join(arr, DEFAULT_STRING_DELIMITER);
  }

  /**
   * Joins the specified array with the defined delimiter.
   *
   * @param arr       The array that provides the elements to be joined.
   * @param delimiter The delimiter used to separate the elements with.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(byte[] arr, String delimiter) {
    return joinArray(arr, delimiter);
  }

  /**
   * Joins the specified list with the {@link #DEFAULT_STRING_DELIMITER}.
   *
   * @param collection The list that provides the elements to be joined.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(Collection<?> collection) {
    return joinArray(collection.toArray(), DEFAULT_STRING_DELIMITER);
  }

  /**
   * Joins the specified collection with the defined delimiter.
   *
   * @param collection The list that provides the elements to be joined.
   * @param delimiter  The delimiter used to separate the elements with.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(Collection<?> collection, String delimiter) {
    return joinArray(collection.toArray(), delimiter);
  }

  /**
   * Joins the specified array with the {@link #DEFAULT_STRING_DELIMITER}.
   *
   * @param arr The array that provides the elements to be joined.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(Object[] arr) {
    return joinArray(arr, DEFAULT_STRING_DELIMITER);
  }

  /**
   * Joins the specified array with the defined delimiter.
   *
   * @param arr       The array that provides the elements to be joined.
   * @param delimiter The delimiter used to separate the elements with.
   * @return A string with all joined elements, separated by the delimiter.
   */
  public static String join(Object[] arr, String delimiter) {
    return joinArray(arr, delimiter);
  }

  public static <T> List<T> toList(T[][] arr) {
    List<T> list = new ArrayList<>();
    for (T[] rows : arr) {
      list.addAll(Arrays.asList(rows));
    }

    return list;
  }

  /**
   * Return true if the array contains the specified value.
   *
   * @param arr   The array that is tested for the existence of the element.
   * @param value The element to check for in the array.
   * @return True if the specified element is in the array; otherwise false.
   */
  public static boolean contains(Object[] arr, Object value) {
    for (Object v : arr) {
      if (value == null && v == null) {
        return true;
      }

      if (v != null && v.equals(value)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Return true if the array contains the specified string argument.
   *
   * @param arr        The array that is tested for the existence of the argument.
   * @param argument   The argument to check for in the array.
   * @param ignoreCase A flag indicating whether the case should be ignored when checking for equality.
   * @return True if the specified argument is in the array; otherwise false.
   */
  public static boolean contains(String[] arr, String argument, boolean ignoreCase) {
    if (arr == null) {
      return false;
    }

    for (String arg : arr) {
      if (arg != null
        && !arg.isEmpty()
        && (ignoreCase && arg.equalsIgnoreCase(argument)
        || !ignoreCase && arg.equals(argument))) {
        return true;
      }
    }

    return false;
  }

  /**
   * Removes the specified deleteItem from the input array and returns a trimmed new array instance without null entries. The resulting array will
   * have a length -1;
   *
   * @param <T>        The element type of the array.
   * @param input      The original array
   * @param deleteItem The item to delete
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
    return result.toArray(
      (T[]) Array.newInstance(input.getClass().getComponentType(), result.size()));
  }

  /**
   * Adds the specified item to the input array and returns a new array instance with the length of the input array +1.
   *
   * @param <T>     The element type of the array.
   * @param input   The original array.
   * @param addItem The item to add.
   * @return A new array with the item to add appended at the end.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] append(T[] input, T addItem) {
    List<T> result = new ArrayList<>(Arrays.asList(input));
    result.add(addItem);

    return result.toArray(
      (T[]) Array.newInstance(input.getClass().getComponentType(), result.size()));
  }

  /**
   * Combines the two specified arrays by only keeping distinct values.
   *
   * @param <T>    The element type of the array.
   * @param first  The first array.
   * @param second The second array.
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

  /**
   * Creates a copy of the specified array.
   *
   * @param <T>      the type of the array elements
   * @param original the array to copy
   * @return a new array that is a copy of the original array
   */
  public static <T> T[] arrayCopy(T[] original) {
    return original.clone();
  }

  /**
   * Converts a list of Integer objects to an array of primitive int values.
   *
   * @param intList the list of Integer objects to convert
   * @return an array of primitive int values
   */
  public static int[] toIntegerArray(List<Integer> intList) {
    Integer[] objArray = intList.toArray(new Integer[0]);
    int[] intArray = new int[objArray.length];
    System.arraycopy(objArray, 0, intArray, 0, objArray.length);
    return intArray;
  }

  /**
   * General method for joining an array. Encapsulated for type safety.
   *
   * @param arr       The array to join.
   * @param separator The separator to use between elements.
   * @return A string with all joined elements, separated by the specified separator.
   */
  private static String joinArray(Object arr, String separator) {
    if (arr == null) {
      return null;
    }

    int len = Array.getLength(arr);
    if (len == 0) {
      return "";
    }

    StringBuilder sb = new StringBuilder(String.valueOf(Array.get(arr, 0)));
    for (int i = 1; i < len; i++) {
      sb.append(separator);
      sb.append(Array.get(arr, i));
    }

    return sb.toString();
  }
}
