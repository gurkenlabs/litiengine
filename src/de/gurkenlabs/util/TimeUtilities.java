package de.gurkenlabs.util;

public class TimeUtilities {
  public static long getYears(long ms) {
    return ms / 1000 / 60 / 60 / 24 / 365;
  }

  public static long getDays(long ms) {
    return ms / 1000 / 60 / 60 / 24 % 365;
  }

  public static long getHours(long ms) {
    return ms / 1000 / 60 / 60 % 24;
  }

  public static long getMinutes(long ms) {
    return ms / 1000 / 60 % 60;
  }

  public static long getSeconds(long ms) {
    return ms / 1000 % 60;
  }

}
