package de.gurkenlabs.litiengine.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Utility class for mathematical operations.
 */
public class MathUtilities {
  /**
   * Private constructor to prevent instantiation.
   * Throws UnsupportedOperationException if called.
   */
  private MathUtilities() {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if two double values are equal within a given epsilon.
   *
   * @param d1 the first double value
   * @param d2 the second double value
   * @param epsilon the tolerance within which the two values are considered equal
   * @return true if the absolute difference between d1 and d2 is less than or equal to epsilon, false otherwise
   */
  public static boolean equals(double d1, double d2, double epsilon) {
    return Math.abs(d1 - d2) <= epsilon;
  }

  /**
   * Rounds a float value to the specified number of decimal places.
   *
   * @param value the float value to be rounded
   * @param places the number of decimal places to round to
   * @return the rounded float value
   */
  public static float round(float value, int places) {
    return (float) round((double) value, places);
  }

  /**
   * Rounds a double value to the specified number of decimal places.
   *
   * @param value the double value to be rounded
   * @param places the number of decimal places to round to
   * @return the rounded double value
   * @throws IllegalArgumentException if the number of decimal places is negative
   */
  public static double round(double value, int places) {
    if (places < 0) {
      throw new IllegalArgumentException();
    }

    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

  /**
   * Clamps a double value to the specified minimum and maximum boundaries.
   *
   * @param value the value to be clamped
   * @param min the minimum value to be accepted
   * @param max the maximum value to be accepted
   * @return a value clamped to the specified boundaries
   */
  public static double clamp(final double value, final double min, final double max) {
    return Math.max(min, Math.min(max, value));
  }

  /**
   * Clamps a float value to the specified minimum and maximum boundaries.
   *
   * @param value the value to be clamped
   * @param min the minimum value to be accepted
   * @param max the maximum value to be accepted
   * @return a value clamped to the specified boundaries
   */
  public static float clamp(final float value, final float min, final float max) {
    return Math.max(min, Math.min(max, value));
  }

  /**
   * Clamps a byte value to the specified minimum and maximum boundaries.
   *
   * @param value the value to be clamped
   * @param min the minimum value to be accepted
   * @param max the maximum value to be accepted
   * @return a value clamped to the specified boundaries
   */
  public static byte clamp(final byte value, final byte min, final byte max) {
    if (value < min) {
      return min;
    }

    if (value > max) {
      return max;
    }

    return value;
  }

  /**
   * Clamps a short value to the specified minimum and maximum boundaries.
   *
   * @param value the value to be clamped
   * @param min the minimum value to be accepted
   * @param max the maximum value to be accepted
   * @return a value clamped to the specified boundaries
   */
  public static short clamp(final short value, final short min, final short max) {
    if (value < min) {
      return min;
    }

    if (value > max) {
      return max;
    }

    return value;
  }

  /**
   * Clamps an int value to the specified minimum and maximum boundaries.
   *
   * @param value the value to be clamped
   * @param min the minimum value to be accepted
   * @param max the maximum value to be accepted
   * @return a value clamped to the specified boundaries
   */
  public static int clamp(final int value, final int min, final int max) {
    if (value < min) {
      return min;
    }

    if (value > max) {
      return max;
    }

    return value;
  }

  /**
   * Clamps a long value to the specified minimum and maximum boundaries.
   *
   * @param value the value to be clamped
   * @param min the minimum value to be accepted
   * @param max the maximum value to be accepted
   * @return a value clamped to the specified boundaries
   */
  public static long clamp(final long value, final long min, final long max) {
    if (value < min) {
      return min;
    }

    if (value > max) {
      return max;
    }

    return value;
  }

  /**
   * Calculates the average of an array of double values.
   *
   * @param numbers the array of double values
   * @return the average of the values in the array
   */
  public static double getAverage(final double[] numbers) {
    return Arrays.stream(numbers).average().orElse(0);
  }

  /**
   * Calculates the average of an array of float values.
   *
   * @param numbers the array of float values
   * @return the average of the values in the array
   */
  public static float getAverage(final float[] numbers) {
    float sum = 0;
    for (final float number : numbers) {
      if (number != 0) {
        sum += number;
      }
    }

    return sum / numbers.length;
  }

  /**
   * Calculates the average of an array of int values.
   *
   * @param numbers the array of int values
   * @return the average of the values in the array
   */
  public static double getAverage(final int[] numbers) {
    return Arrays.stream(numbers).average().orElse(0);
  }

  /**
   * Finds the maximum value in an array of int values.
   *
   * @param numbers the array of int values
   * @return the maximum value in the array
   */
  public static int getMax(final int... numbers) {
    return Arrays.stream(numbers).max().orElse(Integer.MIN_VALUE);
  }

  /**
   * Checks if a double value is an integer.
   *
   * @param value the double value to check
   * @return true if the value is an integer, false otherwise
   */
  public static boolean isInt(final double value) {
    return value == Math.floor(value) && !Double.isInfinite(value);
  }

  /**
   * Checks if an int value is an odd number.
   *
   * @param num the int value to check
   * @return true if the value is odd, false otherwise
   */
  public static boolean isOddNumber(int num) {
    return (num & 1) != 0;
  }

  /**
   * Calculates the percentage of a fraction relative to a value.
   *
   * @param value the total value
   * @param fraction the fraction of the total value
   * @return the percentage of the fraction relative to the total value
   */
  public static int getFullPercent(double value, double fraction) {
    if (value == 0) {
      return 0;
    }

    return (int) ((fraction * 100.0f) / value);
  }

  /**
   * Calculates the percentage of a fraction relative to a value.
   *
   * @param value the total value
   * @param fraction the fraction of the total value
   * @return the percentage of the fraction relative to the total value
   */
  public static double getPercent(double value, double fraction) {
    if (value == 0) {
      return 0;
    }

    return (float) fraction * 100 / value;
  }
}
