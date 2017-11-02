package de.gurkenlabs.litiengine.util;

import org.junit.Assert;
import org.junit.Test;

import de.gurkenlabs.util.TimeUtilities;
import de.gurkenlabs.util.TimeUtilities.TimerFormat;

public class TimeUtilitiesTests {

  @Test
  public void testMillisecondCalculations() {
    long seconds = TimeUtilities.getSeconds(999);
    long minutes = TimeUtilities.getMinutes(59999);
    long hours = TimeUtilities.getHours(3599999);
    long days = TimeUtilities.getDays(86399999);
    Assert.assertEquals(0, seconds);
    Assert.assertEquals(0, minutes);
    Assert.assertEquals(0, hours);
    Assert.assertEquals(0, days);

    long seconds2 = TimeUtilities.getSeconds(1000);
    long minutes2 = TimeUtilities.getMinutes(60000);
    long hours2 = TimeUtilities.getHours(3600000);
    long days2 = TimeUtilities.getDays(86400000);
    Assert.assertEquals(1, seconds2);
    Assert.assertEquals(1, minutes2);
    Assert.assertEquals(1, hours2);
    Assert.assertEquals(1, days2);
  }

  @Test
  public void testToTimerFormat() {
    // test hh:mm:ss

    long ms = 3600123;
    long ms2 = 3599123;
    String timerHH_MM_SS = TimeUtilities.toTimerFormat(ms, TimerFormat.HH_MM_SS);
    String timerHH_MM_SS_000 = TimeUtilities.toTimerFormat(ms, TimerFormat.HH_MM_SS_000);
    String timerMM_SS_000 = TimeUtilities.toTimerFormat(ms2, TimerFormat.MM_SS_000);

    Assert.assertEquals("01:00:00", timerHH_MM_SS);
    Assert.assertEquals("01:00:00.123", timerHH_MM_SS_000);
    Assert.assertEquals("59:59.123", timerMM_SS_000);
  }
}
