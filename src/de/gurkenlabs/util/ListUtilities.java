package de.gurkenlabs.util;

import java.util.List;

public class ListUtilities {

  public static <E> boolean containsInstance(List<E> list, Class<? extends E> clazz) {
    return list.stream().anyMatch(e -> clazz.isInstance(e));
}

}
