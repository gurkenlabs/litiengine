package de.gurkenlabs.litiengine.util;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public final class CollectionUtilities {
  public static <T> T random(Collection<T> coll) {
    int num = (ThreadLocalRandom.current().nextInt(coll.size()));
    for (T t : coll) {
      if (--num < 0) {
        return t;
      }
    }

    return null;
  }

  private CollectionUtilities() {
    throw new UnsupportedOperationException();
  }
}
