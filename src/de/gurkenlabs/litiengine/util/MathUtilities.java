package de.gurkenlabs.litiengine.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class MathUtilities {
  private static Random random = new Random();

  private MathUtilities() {
  }

  public static boolean equals(double d1, double d2, double epsilon) {
    return Math.abs(d1 - d2) <= epsilon;
  }

  public static float round(float value, int places) {
    return (float) round((double) value, places);
  }

  public static double round(double value, int places) {
    if (places < 0) {
      throw new IllegalArgumentException();
    }

    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

  public static double clamp(final double value, final double min, final double max) {
    return Math.max(min, Math.min(max, value));
  }

  public static float clamp(final float value, final float min, final float max) {
    return Math.max(min, Math.min(max, value));
  }

  public static int clamp(final int value, final int min, final int max) {
    if (value < min) {
      return min;
    }

    if (value > max) {
      return max;
    }

    return value;
  }

  public static long clamp(final long value, final long min, final long max) {
    if (value < min) {
      return min;
    }

    if (value > max) {
      return max;
    }

    return value;
  }

  public static double getAverage(final double[] numbers) {
    double sum = 0;
    for (final double number : numbers) {
      if (number != 0) {
        sum += number;
      }
    }

    return sum / numbers.length;
  }

  public static float getAverage(final float[] numbers) {
    float sum = 0;
    for (final float number : numbers) {
      if (number != 0) {
        sum += number;
      }
    }

    return sum / numbers.length;
  }

  public static int getAverage(final int[] numbers) {
    int sum = 0;
    for (final int number : numbers) {
      if (number != 0) {
        sum += number;
      }
    }

    return sum / numbers.length;
  }

  public static int getMax(final int... numbers) {
    int max = Integer.MIN_VALUE;
    for (int i = 0; i < numbers.length; i++) {
      if (numbers[i] > max) {
        max = numbers[i];
      }
    }
    return max;
  }

  /**
   * The index probabilities must sum up to 1;
   *
   * @param indexProbabilities
   *          The index with the probabilities for the related index.
   * @return A random index within the range of the specified array.
   */
  public static int getRandomIndex(final double[] indexProbabilities) {
    final double rnd = random.nextDouble();
    double probSum = 0;
    for (int i = 0; i < indexProbabilities.length; i++) {
      final double newProbSum = probSum + indexProbabilities[i];
      if (rnd >= probSum && rnd < newProbSum) {
        return i;
      }

      probSum = newProbSum;
    }

    return 0;
  }

  public static boolean isInt(final double value) {
    return value == Math.floor(value) && !Double.isInfinite(value);
  }

  public static boolean probabilityIsTrue(final double probability) {
    double rnd = random.nextDouble();
    return rnd < probability;
  }

  public static boolean randomBoolean() {
    return random.nextDouble() < 0.5;
  }

  public static double randomInRange(final double min, final double max) {
    if (min == max) {
      return min;
    }

    if (min > max) {
      throw new IllegalArgumentException("min value is > than max value");
    }

    return min + random.nextDouble() * (max - min);
  }

  public static int randomInRange(final int min, final int max) {
    if (min == max) {
      return min;
    }

    if (min > max) {
      throw new IllegalArgumentException("min value is > than max value");
    }

    return random.nextInt(max - min) + min;
  }

  public static int randomSign() {
    return randomBoolean() ? 1 : -1;
  }

  public static int getFullPercent(double value, double fraction) {
    if (value == 0) {
      return 0;
    }

    return (int) ((fraction * 100.0f) / value);
  }

  public static double getPercent(double value, double fraction) {
    if (value == 0) {
      return 0;
    }

    return (float) fraction * 100 / value;
  }
}
