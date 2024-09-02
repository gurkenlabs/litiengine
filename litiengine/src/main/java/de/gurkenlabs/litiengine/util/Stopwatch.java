package de.gurkenlabs.litiengine.util;

import java.util.function.LongConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for stopwatch-related operations.
 */
public final class Stopwatch {
  private static final Logger log = Logger.getLogger(Stopwatch.class.getName());

  /**
   * Private constructor to prevent instantiation. Throws UnsupportedOperationException if called.
   */
  private Stopwatch() {
    throw new UnsupportedOperationException();
  }

  /**
   * Tracks the execution time of a given operation and logs it to the console.
   *
   * @param name     the name of the operation being tracked
   * @param consumer a LongConsumer that accepts the current time in nanoseconds
   */
  public static void trackInConsole(final String name, final LongConsumer consumer) {
    final long current = System.nanoTime();
    consumer.accept(current);
    log.log(
      Level.INFO,
      "{0} took: {1} ms",
      new Object[] {name, TimeUtilities.nanoToMs(System.nanoTime() - current)});
  }
}
