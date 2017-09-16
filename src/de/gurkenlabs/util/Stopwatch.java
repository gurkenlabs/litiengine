package de.gurkenlabs.util;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Stopwatch {
  private static final Logger log = Logger.getLogger(Stopwatch.class.getName());

  private Stopwatch() {
  }

  public static void trackInConsole(final String name, final Consumer<Long> consumer) {
    final long current = System.nanoTime();
    consumer.accept(current);
    log.log(Level.INFO, "{0} took: {1} ms", new Object[] { name, (System.nanoTime() - current) / 1000000.0 });
  }
}
