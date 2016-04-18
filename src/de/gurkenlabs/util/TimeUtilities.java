package de.gurkenlabs.util;

public class TimeUtilities {
  public static long getYears(final long ms) {
    return ms / 1000 / 60 / 60 / 24 / 365;
  }

  public static long getDays(final long ms) {
    return ms / 1000 / 60 / 60 / 24 % 365;
  }

  public static long getHours(final long ms) {
    return ms / 1000 / 60 / 60 % 24;
  }

  public static long getMinutes(final long ms) {
    return ms / 1000 / 60 % 60;
  }

  public static long getSeconds(final long ms) {
    return ms / 1000 % 60;
  }

}
