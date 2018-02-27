package de.gurkenlabs.litiengine.util;

import java.util.concurrent.TimeUnit;

public final class TimeUtilities {
  public enum TimerFormat {
    UNDEFINED, HH_MM_SS, MM_SS_000, MM_SS_0, HH_MM_SS_000, DD_HH_MM;

    public String toFormatString() {
      switch (this) {
      case HH_MM_SS:
        return "%02d:%02d:%02d";
      case HH_MM_SS_000:
        return "%02d:%02d:%02d.%03d";
      case MM_SS_000:
        return "%02d:%02d.%03d";
      case MM_SS_0:
        return "%02d:%02d.%01d";
      default:
        return null;
      }
    }
  }

  private TimeUtilities() {
  }

  public static long getDays(final long ms) {
    return TimeUnit.MILLISECONDS.toDays(ms);
  }

  public static long getHours(final long ms) {
    return TimeUnit.MILLISECONDS.toHours(ms);
  }

  public static long getMinutes(final long ms) {
    return TimeUnit.MILLISECONDS.toMinutes(ms);
  }

  public static long getSeconds(final long ms) {
    return TimeUnit.MILLISECONDS.toSeconds(ms);
  }

  public static long getRemainingDays(final long ms) {
    return ms / 1000 / 60 / 60 / 24 % 365;
  }

  public static long getRemainingHours(final long ms) {
    return ms / 1000 / 60 / 60 % 24;
  }

  public static long getRemainingMinutes(final long ms) {
    return ms / 1000 / 60 % 60;
  }

  public static long getRemainingSeconds(final long ms) {
    return ms / 1000 % 60;
  }

  public static long getRemainingMilliSeconds(long ms) {
    return ms % 1000;
  }

  public static String toTimerFormat(long duration, TimerFormat format) {
    long h = getRemainingHours(duration);
    long m = getRemainingMinutes(duration);
    long s = getRemainingSeconds(duration);
    long ms = getRemainingMilliSeconds(duration);

    switch (format) {
    case HH_MM_SS:
      return String.format(format.toFormatString(), h, m, s);
    case HH_MM_SS_000:
      return String.format(format.toFormatString(), h, m, s, ms);
    case MM_SS_000:
      return String.format(format.toFormatString(), m, s, ms);
    case MM_SS_0:
      return String.format(format.toFormatString(), m, s, ms / 100);
    case UNDEFINED:
    default:
      return Long.toString(ms);
    }
  }
}
