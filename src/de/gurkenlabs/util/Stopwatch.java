package de.gurkenlabs.util;

import java.util.function.Consumer;

public class Stopwatch {
  public static void trackInConsole(final String name, final Consumer<Long> consumer) {
    final long current = System.nanoTime();
    consumer.accept(current);
    System.out.println(name + " took: " + (System.nanoTime() - current) / 1000000.0 + "ms");
  }
}
