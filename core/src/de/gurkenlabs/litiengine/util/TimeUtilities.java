package de.gurkenlabs.litiengine.util;

import java.util.concurrent.TimeUnit;

public final class TimeUtilities {
  public enum TimerFormat {
    UNDEFINED(null),
    HH_MM_SS("%02d:%02d:%02d"),
    MM_SS_000("%02d:%02d.%03d"),
    MM_SS_0("%02d:%02d.%01d"),
    SS_000("%02d.%03d"),
    SS_00("%02d.%02d"),
    SS_0("%02d.%01d"),
    S_000("%01d.%03d"),
    S_00("%01d.%02d"),
    S_0("%01d.%01d"),
    HH_MM_SS_000("%02d:%02d:%02d.%03d"),
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

  public static double nanoToMs(final long nano) {
    return nano / 1000000.0;
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
        return String.format(format.getFormatString(), h, m, s);
      case HH_MM_SS_000:
        return String.format(format.getFormatString(), h, m, s, ms);
      case HH_MM_SS_0:
        return String.format(format.getFormatString(), h, m, s, ms / 100);
      case MM_SS_000:
        return String.format(format.getFormatString(), m, s, ms);
      case MM_SS_0:
        return String.format(format.getFormatString(), m, s, ms / 100);
      case SS_000:
      case S_000:
        return String.format(format.getFormatString(), s, ms);
      case SS_00:
      case S_00:
        return String.format(format.getFormatString(), s, ms / 10);
      case SS_0:
      case S_0:
        return String.format(format.getFormatString(), s, ms / 100);
      case UNDEFINED:
      default:
        return Long.toString(ms);
    }
  }
}
