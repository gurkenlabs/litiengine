package de.gurkenlabs.litiengine.util;

import java.util.function.LongConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Stopwatch {
  private static final Logger log = Logger.getLogger(Stopwatch.class.getName());

  private Stopwatch() {
    throw new UnsupportedOperationException();
  }

  public static void trackInConsole(final String name, final LongConsumer consumer) {
    final long current = System.nanoTime();
    consumer.accept(current);
    log.log(
        Level.INFO,
        "{0} took: {1} ms",
        new Object[] {name, TimeUtilities.nanoToMs(System.nanoTime() - current)});
  }
}
