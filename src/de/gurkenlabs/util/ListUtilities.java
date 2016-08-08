package de.gurkenlabs.util;

import java.util.List;

public class ListUtilities {

  public static <E> boolean containsInstance(final List<E> list, final Class<? extends E> clazz) {
    return list.stream().anyMatch(e -> clazz.isInstance(e));
  }

}
