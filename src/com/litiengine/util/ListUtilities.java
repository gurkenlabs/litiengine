package com.litiengine.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListUtilities {
  private ListUtilities() {
    throw new UnsupportedOperationException();
  }

  public static <E> boolean containsInstance(final List<E> list, final Class<? extends E> clazz) {
    return list.stream().anyMatch(clazz::isInstance);
  }

  public static List<Integer> getIntList(int... values) {
    return Arrays.stream(values).boxed().collect(Collectors.toList());
  }
}
