package de.gurkenlabs.util;

import java.util.Random;

public class MathUtilities {
  private static Random RANDOM = new Random();

  /**
   * The index probabilities must sum up to 1;
   *
   * @param indexProbabilities
   * @return
   */
  public static int getRandomIndex(final double[] indexProbabilities) {
    final double rnd = RANDOM.nextDouble();
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

  public static double getAverage(double[] numbers) {
    double sum = 0;
    for (double number : numbers) {
      if (number != 0) {
        sum += number;
      }
    }

    return sum / numbers.length;
  }

  public static float getAverage(float[] numbers) {
    float sum = 0;
    for (float number : numbers) {
      if (number != 0) {
        sum += number;
      }
    }

    return sum / numbers.length;
  }

  public static int getAverage(int[] numbers) {
    int sum = 0;
    for (int number : numbers) {
      if (number != 0) {
        sum += number;
      }
    }

    return sum / numbers.length;
  }

  public static boolean probabilityIsTrue(final double probability) {
    return RANDOM.nextDouble() < probability;
  }

  public static boolean randomBoolean() {
    return RANDOM.nextDouble() < 0.5;
  }

  public static double randomInRange(final double min, final double max) {
    if (min == max) {
      return min;
    }

    if (min > max) {
      throw new IllegalArgumentException("min value is > than max value");
    }

    return min + RANDOM.nextDouble() * (max - min);
  }

  public static int randomInRange(final int min, final int max) {
    if (min == max) {
      return min;
    }

    if (min > max) {
      throw new IllegalArgumentException("min value is > than max value");
    }

    return (int) (min + RANDOM.nextDouble() * (max - min));
  }

  public static int randomSign() {
    return randomBoolean() ? 1 : -1;
  }

  public static boolean isInt(double value) {
    return (value == Math.floor(value)) && !Double.isInfinite(value);
  }

  public static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }

  public static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  public static int clamp(int value, int min, int max) {
    if (value < min) {
      return min;
    }

    if (value > max) {
      return max;
    }

    return value;
  }

  public static long clamp(long value, long min, long max) {
    if (value < min) {
      return min;
    }

    if (value > max) {
      return max;
    }

    return value;
  }
}
