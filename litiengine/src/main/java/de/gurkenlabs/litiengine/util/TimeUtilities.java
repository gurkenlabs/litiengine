package de.gurkenlabs.litiengine.util;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for various time-related operations. This class cannot be instantiated.
 */
public final class TimeUtilities {
  /**
   * Enum representing various timer formats. Each format is associated with a specific format string.
   */
  public enum TimerFormat {
    /**
     * Undefined timer format.
     */
    UNDEFINED(null),

    /**
     * Timer format in hours, minutes, and seconds (HH:MM:SS).
     */
    HH_MM_SS("%02d:%02d:%02d"),

    /**
     * Timer format in minutes, seconds, and milliseconds (MM:SS.000).
     */
    MM_SS_000("%02d:%02d.%03d"),

    /**
     * Timer format in minutes, seconds, and tenths of a second (MM:SS.0).
     */
    MM_SS_0("%02d:%02d.%01d"),

    /**
     * Timer format in seconds and milliseconds (SS.000).
     */
    SS_000("%02d.%03d"),

    /**
     * Timer format in seconds and hundredths of a second (SS.00).
     */
    SS_00("%02d.%02d"),

    /**
     * Timer format in seconds and tenths of a second (SS.0).
     */
    SS_0("%02d.%01d"),

    /**
     * Timer format in seconds and milliseconds (S.000).
     */
    S_000("%01d.%03d"),

    /**
     * Timer format in seconds and hundredths of a second (S.00).
     */
    S_00("%01d.%02d"),

    /**
     * Timer format in seconds and tenths of a second (S.0).
     */
    S_0("%01d.%01d"),

    /**
     * Timer format in hours, minutes, seconds, and milliseconds (HH:MM:SS.000).
     */
    HH_MM_SS_000("%02d:%02d:%02d.%03d"),

    /**
     * Timer format in hours, minutes, seconds, and tenths of a second (HH:MM:SS.0).
     */
    HH_MM_SS_0("%02d:%02d:%02d.%01d");

    private final String formatString;

    TimerFormat(String formatString) {
      this.formatString = formatString;
    }

    public String getFormatString() {
      return formatString;
    }
  }

  private TimeUtilities() {
    throw new UnsupportedOperationException();
  }

  /**
   * Converts nanoseconds to milliseconds.
   *
   * @param nano the duration in nanoseconds
   * @return the duration in milliseconds
   */
  public static double nanoToMs(final long nano) {
    return nano / 1000000.0;
  }

  /**
   * Converts milliseconds to days.
   *
   * @param ms the duration in milliseconds
   * @return the duration in days
   */
  public static long getDays(final long ms) {
    return TimeUnit.MILLISECONDS.toDays(ms);
  }

  /**
   * Converts milliseconds to hours.
   *
   * @param ms the duration in milliseconds
   * @return the duration in hours
   */
  public static long getHours(final long ms) {
    return TimeUnit.MILLISECONDS.toHours(ms);
  }

  /**
   * Converts milliseconds to minutes.
   *
   * @param ms the duration in milliseconds
   * @return the duration in minutes
   */
  public static long getMinutes(final long ms) {
    return TimeUnit.MILLISECONDS.toMinutes(ms);
  }

  /**
   * Converts milliseconds to seconds.
   *
   * @param ms the duration in milliseconds
   * @return the duration in seconds
   */
  public static long getSeconds(final long ms) {
    return TimeUnit.MILLISECONDS.toSeconds(ms);
  }

  /**
   * Gets the remaining days from the given milliseconds.
   *
   * @param ms the duration in milliseconds
   * @return the remaining days
   */
  public static long getRemainingDays(final long ms) {
    return ms / 1000 / 60 / 60 / 24 % 365;
  }

  /**
   * Gets the remaining hours from the given milliseconds.
   *
   * @param ms the duration in milliseconds
   * @return the remaining hours
   */
  public static long getRemainingHours(final long ms) {
    return ms / 1000 / 60 / 60 % 24;
  }

  /**
   * Gets the remaining minutes from the given milliseconds.
   *
   * @param ms the duration in milliseconds
   * @return the remaining minutes
   */
  public static long getRemainingMinutes(final long ms) {
    return ms / 1000 / 60 % 60;
  }

  /**
   * Gets the remaining seconds from the given milliseconds.
   *
   * @param ms the duration in milliseconds
   * @return the remaining seconds
   */
  public static long getRemainingSeconds(final long ms) {
    return ms / 1000 % 60;
  }

  /**
   * Gets the remaining milliseconds from the given milliseconds.
   *
   * @param ms the duration in milliseconds
   * @return the remaining milliseconds
   */
  public static long getRemainingMilliSeconds(long ms) {
    return ms % 1000;
  }

  /**
   * Converts a duration to a formatted timer string based on the specified timer format.
   *
   * @param duration the duration in milliseconds
   * @param format   the timer format to use
   * @return the formatted timer string
   */
  public static String toTimerFormat(long duration, TimerFormat format) {
    long h = getRemainingHours(duration);
    long m = getRemainingMinutes(duration);
    long s = getRemainingSeconds(duration);
    long ms = getRemainingMilliSeconds(duration);

    return switch (format) {
      case HH_MM_SS -> String.format(format.getFormatString(), h, m, s);
      case HH_MM_SS_000 -> String.format(format.getFormatString(), h, m, s, ms);
      case HH_MM_SS_0 -> String.format(format.getFormatString(), h, m, s, ms / 100);
      case MM_SS_000 -> String.format(format.getFormatString(), m, s, ms);
      case MM_SS_0 -> String.format(format.getFormatString(), m, s, ms / 100);
      case SS_000, S_000 -> String.format(format.getFormatString(), s, ms);
      case SS_00, S_00 -> String.format(format.getFormatString(), s, ms / 10);
      case SS_0, S_0 -> String.format(format.getFormatString(), s, ms / 100);
      default -> Long.toString(ms);
    };
  }
}
