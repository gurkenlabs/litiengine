package de.gurkenlabs.util.logging;

import java.util.function.Consumer;

public class Stopwatch {
  public static void trackInConsole(String name, Consumer<Long> consumer) {
    long current = System.nanoTime();
    consumer.accept(current);
    System.out.println(name + " took: " + ((System.nanoTime() - current) / 1000000.0) + "ms");
  }
}
