package de.gurkenlabs.litiengine.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtilities {
  private ListUtilities() {
    throw new UnsupportedOperationException();
  }

  public static <E> boolean containsInstance(final List<E> list, final Class<? extends E> clazz) {
    return list.stream().anyMatch(clazz::isInstance);
  }

  public static List<Integer> getIntList(int... values) {
    List<Integer> list = new ArrayList<>();
    for (int upKey : values) {
      list.add(upKey);
    }

    return list;
  }
}
