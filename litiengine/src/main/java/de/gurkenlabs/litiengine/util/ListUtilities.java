package de.gurkenlabs.litiengine.util;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for various list operations. This class cannot be instantiated.
 */
public class ListUtilities {
  private ListUtilities() {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if the list contains an instance of the specified class.
   *
   * @param <E>   the type of elements in the list
   * @param list  the list to check
   * @param clazz the class to check for instances of
   * @return true if the list contains an instance of the specified class, false otherwise
   */
  public static <E> boolean containsInstance(final List<E> list, final Class<? extends E> clazz) {
    return list.stream().anyMatch(clazz::isInstance);
  }

  /**
   * Converts an array of int values to a list of Integer objects.
   *
   * @param values the array of int values
   * @return a list of Integer objects
   */
  public static List<Integer> getIntList(int... values) {
    return Arrays.stream(values).boxed().toList();
  }
}
