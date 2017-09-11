package de.gurkenlabs.litiengine.util;

import org.junit.Assert;
import org.junit.Test;

import de.gurkenlabs.util.MathUtilities;

public class MathUtilitiesTests {

  @Test
  public void testIntClamp() {
    int toLow = 4;
    int toHigh = 11;
    int inRange = 6;

    Assert.assertEquals(5, MathUtilities.clamp(toLow, 5, 10));
    Assert.assertEquals(10, MathUtilities.clamp(toHigh, 5, 10));
    Assert.assertEquals(6, MathUtilities.clamp(inRange, 5, 10));
  }

  @Test
  public void testLongClamp() {
    long toLow = 4000000000L;
    long toHigh = 11000000000L;
    long inRange = 6600000000L;

    Assert.assertEquals(5000000000L, MathUtilities.clamp(toLow, 5000000000L, 10000000000L));
    Assert.assertEquals(10000000000L, MathUtilities.clamp(toHigh, 5000000000L, 10000000000L));
    Assert.assertEquals(6600000000L, MathUtilities.clamp(inRange, 5000000000L, 10000000000L));
  }

  @Test
  public void testDoubleClamp() {
    double toLow = 4.3;
    double toHigh = 10.4;
    double inRange = 6.6;

    Assert.assertEquals(5.0, MathUtilities.clamp(toLow, 5, 10), 0.0001);
    Assert.assertEquals(10.0, MathUtilities.clamp(toHigh, 5, 10), 0.0001);
    Assert.assertEquals(6.6, MathUtilities.clamp(inRange, 5, 10), 0.0001);
  }

  @Test
  public void testFloatClamp() {
    float toLow = 4.3f;
    float toHigh = 10.4f;
    float inRange = 6.6f;

    Assert.assertEquals(5.0f, MathUtilities.clamp(toLow, 5, 10), 0.0001);
    Assert.assertEquals(10.0f, MathUtilities.clamp(toHigh, 5, 10), 0.0001);
    Assert.assertEquals(6.6f, MathUtilities.clamp(inRange, 5, 10), 0.0001);
  }

  @Test
  public void testGetAverageInt() {
    int avg = MathUtilities.getAverage(new int[] { 2, 2, 1, 1, 1, 2 });
    int avg2 = MathUtilities.getAverage(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

    Assert.assertEquals(1, avg);
    Assert.assertEquals(5, avg2);
  }

  @Test
  public void testGetAverageDouble() {
    double avg = MathUtilities.getAverage(new double[] { 2, 2, 1, 1, 1, 2 });
    double avg2 = MathUtilities.getAverage(new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

    Assert.assertEquals(1.5, avg, 0.0001);
    Assert.assertEquals(5.5, avg2, 0.0001);
  }

  @Test
  public void testGetAverageFloat() {
    float avg = MathUtilities.getAverage(new float[] { 2, 2, 1, 1, 1, 2 });
    float avg2 = MathUtilities.getAverage(new float[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

    Assert.assertEquals(1.5f, avg, 0.0001);
    Assert.assertEquals(5.5f, avg2, 0.0001);
  }

  @Test
  public void testRandomInRange() {
    for (int i = 0; i < 100; i++) {
      double rnd = MathUtilities.randomInRange(0.0, 10);
      int rndInt = MathUtilities.randomInRange(0, 10);

      Assert.assertTrue(rnd >= 0 && rnd < 10);
      Assert.assertTrue(rndInt >= 0 && rndInt < 10);
    }
  }

  @Test
  public void testRandomSign() {
    for (int i = 0; i < 100; i++) {
      int rnd = MathUtilities.randomSign();
      Assert.assertTrue(rnd == 1 || rnd == -1);
    }
  }
}
