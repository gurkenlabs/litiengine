package de.gurkenlabs.litiengine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MathUtilitiesTests {

  @Test
  void testRound() {
    float value1 = 4.3f;
    float value2 = 10.4f;
    float value3 = 6.6f;

    assertEquals(4, MathUtilities.round(value1, 0), 0.0001);
    assertEquals(10.4, MathUtilities.round(value2, 1), 0.0001);
    assertEquals(6.6, MathUtilities.round(value3, 2), 0.0001);
  }

  @Test
  void testGetAverageInt() {
    double avg = MathUtilities.getAverage(new int[] {2, 2, 1, 1, 1, 2});
    double avg2 = MathUtilities.getAverage(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

    assertEquals(1.5, avg);
    assertEquals(5.5, avg2);
  }

  @Test
  void testGetAverageDouble() {
    double avg = MathUtilities.getAverage(new double[] {2, 2, 1, 1, 1, 2});
    double avg2 = MathUtilities.getAverage(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

    assertEquals(1.5, avg, 0.0001);
    assertEquals(5.5, avg2, 0.0001);
  }

  @Test
  void testGetAverageFloat() {
    float avg = MathUtilities.getAverage(new float[] {2, 2, 1, 1, 1, 2});
    float avg2 = MathUtilities.getAverage(new float[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

    assertEquals(1.5f, avg, 0.0001);
    assertEquals(5.5f, avg2, 0.0001);
  }

  @Test
  void testGetMaxInt() {
    int max = MathUtilities.getMax(new int[] {2, 2, 1, 1, 1, 2});
    int max2 = MathUtilities.getMax(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

    assertEquals(2, max);
    assertEquals(10, max2);
  }

  @Test
  void testIsOddNumber() {
    int oddNumber = 1;
    int evenNumber = 2;

    assertTrue(MathUtilities.isOddNumber(oddNumber));
    assertFalse(MathUtilities.isOddNumber(evenNumber));
  }

  @ParameterizedTest(name = "testGetFullPercent percent={0}, fraction={2}, expectedValue={3}")
  @CsvSource({"4.3d, 2.15d, 50.0d", "0, 2.15d, 0", "0, 2.6d, 0", "10.4, 2.6d, 25.0d"})
  void testGetFullPercent(double percent, double fraction, double expectedValue) {
    assertEquals(expectedValue, MathUtilities.getFullPercent(percent, fraction));
  }

  @Test
  void testGetPercent() {
    double percent1 = 4.3;
    double percent2 = 0.0;
    double percent3 = 0;
    double percent4 = 10.4;

    double fraction1 = 2.15;
    double fraction2 = 2.6;

    assertEquals(50.0, MathUtilities.getPercent(percent1, fraction1), 0.0001);
    assertEquals(0, MathUtilities.getPercent(percent2, fraction1));
    assertEquals(0, MathUtilities.getPercent(percent3, fraction2));
    assertEquals(25.0, MathUtilities.getPercent(percent4, fraction2), 0.0001);
  }
}
