package de.gurkenlabs.litiengine.util;

import org.junit.Assert;
import org.junit.Test;

import de.gurkenlabs.util.TimeUtilities;

public class TimeUtilitiesTests {

  @Test
  public void testMillisecondCalculations() {
    long seconds = TimeUtilities.getSeconds(999);
    long minutes = TimeUtilities.getMinutes(59999);
    long hours = TimeUtilities.getHours(3599999);
    long days = TimeUtilities.getDays(86399999);
    long years = TimeUtilities.getYears(31535999999L);
    Assert.assertEquals(0, seconds);
    Assert.assertEquals(0, minutes);
    Assert.assertEquals(0, hours);
    Assert.assertEquals(0, days);
    Assert.assertEquals(0, years);

    long seconds2 = TimeUtilities.getSeconds(1000);
    long minutes2 = TimeUtilities.getMinutes(60000);
    long hours2 = TimeUtilities.getHours(3600000);
    long days2 = TimeUtilities.getDays(86400000);
    long years2 = TimeUtilities.getYears(31536000000L);
    Assert.assertEquals(1, seconds2);
    Assert.assertEquals(1, minutes2);
    Assert.assertEquals(1, hours2);
    Assert.assertEquals(1, days2);
    Assert.assertEquals(1, years2);
  }
}
