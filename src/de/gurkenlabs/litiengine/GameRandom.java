package de.gurkenlabs.litiengine;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

/**
 * A random number generator instance that provides enhanced functionalities for the java default <code>Random</code> implementation.
 */
@SuppressWarnings("serial")
public final class GameRandom extends java.util.Random {
  private static final String INVALID_BOUNDS_ERROR = "min value is > than max value";
  private static final String ARRAY_MUST_NOT_BE_EMPTY = "array to chose an element from must not be null or empty.";
  private static final String INVALID_AMOUNT_FOR_SAMPLING_WITHOUT_REPLACEMENT = "amount must be <= the specified array length for sampling without replacement.";

  GameRandom() {
  }

  /**
   * Sets the seed of this random number generator using a
   * {@code String} seed.
   *
   * @param seed
   *          The initial seed.
   * 
   * @see #setSeed(long)
   */
  public void setSeed(String seed) {
    this.setSeed(seed.hashCode());
  }

  public <T> T[] sample(final T[] array, int amount, boolean replacement) {
    if (!replacement && array.length < amount) {
      throw new IllegalArgumentException(INVALID_AMOUNT_FOR_SAMPLING_WITHOUT_REPLACEMENT);
    }

    //declare array, cast to (T[]) that was determined using reflection, use java.lang.reflect to create a new instance of an Array(of arrayType variable, and the same length as the original
    @SuppressWarnings("unchecked")
    T[] sampled = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), amount);

    if (!replacement) {
      T[] copiedArray = ArrayUtilities.arrayCopy(array);
      this.shuffle(copiedArray);

      //Use System and arraycopy to copy the array
      System.arraycopy(copiedArray, 0, sampled, 0, amount);

      return sampled;
    }

    for (int i = 0; i < amount; i++) {
      sampled[i] = this.choose(array);
    }

    return sampled;
  }

  public <T> Collection<T> sample(final Collection<T> collection, int amount, boolean replacement) {
    if (!replacement && collection.size() < amount) {
      throw new IllegalArgumentException(INVALID_AMOUNT_FOR_SAMPLING_WITHOUT_REPLACEMENT);
    }

    if (!replacement) {
      List<T> copied = new ArrayList<>(collection);
      this.shuffle(copied);

      return copied.stream().limit(amount).collect(Collectors.toList());
    }

    List<T> copied = new ArrayList<>(amount);

    for (int i = 0; i < amount; i++) {
      copied.add(this.choose(collection));
    }

    return copied;
  }

  /**
   * Chooses a pseudo-random element from the specified array.
   * 
   * @param <T>
   *          The type of the elements in the array.
   * @param array
   *          The array to choose from.
   * @return A pseudo-random element from the array or null if the array is empty.
   */
  public <T> T choose(T[] array) {
    if (array == null || array.length == 0) {
      return null;
    }

    return array[this.nextInt(array.length)];
  }

  /**
   * Chooses a pseudo-random element from the specified array.
   * 
   * @param array
   *          The array to choose from.
   * @return A pseudo-random element from the array or 0 if the array is empty.
   * 
   * @throws IllegalArgumentException
   *           When the specified array is null or empty.
   */
  public int choose(int... array) {
    if (array == null || array.length == 0) {
      throw new IllegalArgumentException(ARRAY_MUST_NOT_BE_EMPTY);
    }

    return array[this.nextInt(array.length)];
  }

  /**
   * Chooses a pseudo-random element from the specified array.
   * 
   * @param array
   *          The array to choose from.
   * @return A pseudo-random element from the array or 0 if the array is empty.
   * 
   * @throws IllegalArgumentException
   *           When the specified array is null or empty.
   */
  public long choose(long... array) {
    if (array == null || array.length == 0) {
      throw new IllegalArgumentException(ARRAY_MUST_NOT_BE_EMPTY);
    }

    return array[this.nextInt(array.length)];
  }

  /**
   * Chooses a pseudo-random element from the specified array.
   * 
   * @param array
   *          The array to choose from.
   * @return A pseudo-random element from the array or 0 if the array is empty.
   * 
   * @throws IllegalArgumentException
   *           When the specified array is null or empty.
   */
  public double choose(double... array) {
    if (array == null || array.length == 0) {
      throw new IllegalArgumentException(ARRAY_MUST_NOT_BE_EMPTY);
    }

    return array[this.nextInt(array.length)];
  }

  /**
   * Chooses a pseudo-random element from the specified collection.
   * 
   * @param <T>
   *          The type of the elements in the collection.
   * @param coll
   *          The collection to choose from.
   * @return A pseudo-random element from the array or null if the collection is empty.
   */
  public <T> T choose(Collection<T> coll) {
    if (coll == null) {
      return null;
    }

    int num = (this.nextInt(coll.size()));
    for (T t : coll) {
      if (--num < 0) {
        return t;
      }
    }

    return null;
  }

  /**
   * Shuffles the elements in the specified array.
   * 
   * @param <T>
   *          The type of the elements in the collection.
   * @param array
   *          The array to be shuffled.
   */
  public <T> void shuffle(T[] array) {
    for (int i = 0; i < array.length; i++) {
      int randomPosition = this.nextInt(array.length);
      T temp = array[i];
      array[i] = array[randomPosition];
      array[randomPosition] = temp;
    }
  }

  /**
   * Shuffles the elements in the specified array.
   * 
   * @param array
   *          The array to be shuffled.
   */
  public void shuffle(int[] array) {
    for (int i = 0; i < array.length; i++) {
      int randomPosition = this.nextInt(array.length);
      int temp = array[i];
      array[i] = array[randomPosition];
      array[randomPosition] = temp;
    }
  }

  /**
   * Shuffles the elements in the specified array.
   * 
   * @param array
   *          The array to be shuffled.
   */
  public void shuffle(long[] array) {
    for (int i = 0; i < array.length; i++) {
      int randomPosition = this.nextInt(array.length);
      long temp = array[i];
      array[i] = array[randomPosition];
      array[randomPosition] = temp;
    }
  }

  /**
   * Shuffles the elements in the specified array.
   * 
   * @param array
   *          The array to be shuffled.
   */
  public void shuffle(double[] array) {
    for (int i = 0; i < array.length; i++) {
      int randomPosition = this.nextInt(array.length);
      double temp = array[i];
      array[i] = array[randomPosition];
      array[randomPosition] = temp;
    }
  }

  /**
   * Shuffles the elements in the specified collection.
   * 
   * @param <T>
   *          The type of the elements in the collection.
   * @param coll
   *          The collection to be shuffled.
   */
  public <T> void shuffle(List<T> coll) {
    Collections.shuffle(coll, this);
  }

  /**
   * Gets a random algebraic sign that can be used to multiply values with it.
   * 
   * <p>
   * This either returns 1 or -1 depending on the random outcome.
   * </p>
   * 
   * @return A random sign for algebraic operations.
   */
  public int nextSign() {
    return this.nextBoolean() ? 1 : -1;
  }

  /**
   * Shuffles the algebraic sign of the specified int value.
   * 
   * @param value
   *          The value to shuffle.
   * 
   * @return Either the specified value; or its negative equivalent (multiplied by -1).
   * 
   * @see #nextSign()
   */
  public int shuffleSign(int value) {
    return value * this.nextSign();
  }

  /**
   * Shuffles the algebraic sign of the specified float value.
   * 
   * @param value
   *          The value to shuffle.
   * 
   * @return Either the specified value; or its negative equivalent (multiplied by -1).
   * 
   * @see #nextSign()
   */
  public float shuffleSign(float value) {
    return value * this.nextSign();
  }

  /**
   * Shuffles the algebraic sign of the specified long value.
   * 
   * @param value
   *          The value to shuffle.
   * 
   * @return Either the specified value; or its negative equivalent (multiplied by -1).
   * 
   * @see #nextSign()
   */
  public long shuffleSign(long value) {
    return value * this.nextSign();
  }

  /**
   * Shuffles the algebraic sign of the specified double value.
   * 
   * @param value
   *          The value to shuffle.
   * 
   * @return Either the specified value; or its negative equivalent (multiplied by -1).
   * 
   * @see #nextSign()
   */
  public double shuffleSign(double value) {
    return value * this.nextSign();
  }

  /**
   * Returns a pseudo-random {@code long} value between zero (inclusive)
   * and the specified bound (exclusive).
   *
   * @param bound
   *          the upper bound (exclusive). Must be positive.
   * @return a pseudo-random {@code long} value between zero
   *         (inclusive) and the bound (exclusive)
   * @throws IllegalArgumentException
   *           if {@code bound} is not positive
   */
  public long nextLong(final long bound) {
    return this.nextLong(0, bound);
  }

  /**
   * Returns a pseudo-random {@code long} value between the specified
   * origin (inclusive) and the specified bound (exclusive).
   *
   * @param min
   *          the least value returned
   * @param bound
   *          the upper bound (exclusive)
   * @return a pseudo-random {@code long} value between the origin
   *         (inclusive) and the bound (exclusive)
   * @throws IllegalArgumentException
   *           if {@code origin} is greater than {@code bound}
   */
  public long nextLong(final long min, final long bound) {
    if (min == bound) {
      return min;
    }

    if (min > bound) {
      throw new IllegalArgumentException(INVALID_BOUNDS_ERROR);
    }

    return min + this.nextLong() * (bound - min);
  }

  /**
   * Returns a pseudo-random {@code double} value between zero (inclusive)
   * and the specified bound (exclusive).
   *
   * @param bound
   *          the upper bound (exclusive). Must be positive.
   * @return a pseudo-random {@code double} value between zero
   *         (inclusive) and the bound (exclusive)
   * @throws IllegalArgumentException
   *           if {@code bound} is not positive
   */
  public double nextDouble(final double bound) {
    return this.nextDouble(0, bound);
  }

  /**
   * Returns a pseudo-random {@code double} value between the specified
   * origin (inclusive) and the specified bound (exclusive).
   *
   * @param min
   *          the least value returned
   * @param bound
   *          the upper bound (exclusive)
   * @return a pseudo-random {@code double} value between the origin
   *         (inclusive) and the bound (exclusive)
   * @throws IllegalArgumentException
   *           if {@code origin} is greater than {@code bound}
   */
  public double nextDouble(final double min, final double bound) {
    if (min == bound) {
      return min;
    }

    if (min > bound) {
      throw new IllegalArgumentException(INVALID_BOUNDS_ERROR);
    }

    return min + this.nextDouble() * (bound - min);
  }

  /**
   * Returns a pseudo-random {@code float} value between zero (inclusive)
   * and the specified bound (exclusive).
   *
   * @param bound
   *          the upper bound (exclusive). Must be positive.
   * @return a pseudo-random {@code float} value between zero
   *         (inclusive) and the bound (exclusive)
   * @throws IllegalArgumentException
   *           if {@code bound} is not positive
   */
  public float nextFloat(final float bound) {
    return this.nextFloat(0, bound);
  }

  /**
   * Returns a pseudo-random {@code float} value between the specified
   * origin (inclusive) and the specified bound (exclusive).
   *
   * @param min
   *          the least value returned
   * @param bound
   *          the upper bound (exclusive)
   * @return a pseudo-random {@code float} value between the origin
   *         (inclusive) and the bound (exclusive)
   * @throws IllegalArgumentException
   *           if {@code origin} is greater than {@code bound}
   */
  public float nextFloat(final float min, final float bound) {
    if (min == bound) {
      return min;
    }

    if (min > bound) {
      throw new IllegalArgumentException(INVALID_BOUNDS_ERROR);
    }

    return min + this.nextFloat() * (bound - min);
  }

  /**
   * Returns a pseudo-random {@code int} value between the specified
   * origin (inclusive) and the specified bound (exclusive).
   *
   * @param min
   *          the least value returned
   * @param bound
   *          the upper bound (exclusive)
   * @return a pseudo-random {@code int} value between the origin
   *         (inclusive) and the bound (exclusive)
   * @throws IllegalArgumentException
   *           if {@code origin} is greater than {@code bound}
   */
  public int nextInt(final int min, final int bound) {
    if (min == bound) {
      return min;
    }

    if (min > bound) {
      throw new IllegalArgumentException(INVALID_BOUNDS_ERROR);
    }

    return this.nextInt(bound - min) + min;
  }

  public <T extends Enum<?>> T next(Class<T> clazz) {
    int x = this.nextInt(clazz.getEnumConstants().length);
    return clazz.getEnumConstants()[x];
  }

  /**
   * Probes a pseudo-random value between 0.0 and 1.0 and checks whether it matches the specified probability.
   * 
   * <p>
   * Example: if the specified probability is 0.5, the sampled value needs to be less than or equal to the specified value
   * in order for this method to return true.
   * </p>
   * 
   * @param probability
   *          The probability to check.
   * @return True if the sampled value matches the probability; otherwise false.
   */
  public boolean probe(final double probability) {
    double rnd = this.nextDouble();
    return rnd <= probability;
  }

  /**
   * Returns a pseudo-random index that is distributed by the weights of the defined probability array.
   * The index probabilities must sum up to 1;
   *
   * @param indexProbabilities
   *          The index with the probabilities for the related index.
   * @return A random index within the range of the specified array.
   */
  public int getIndex(final double[] indexProbabilities) {
    final double rnd = this.nextDouble();
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

  /**
   * Gets a pseudo-random location within the specified boundaries.
   * 
   * @param x
   *          The min-x coordinate of the boundaries.
   * @param y
   *          The min-y coordinate of the boundaries.
   * @param width
   *          The width of the boundaries.
   * @param height
   *          The height of the boundaries.
   * @return A pseudo-random location within the specified bounds.
   */
  public Point2D getLocation(final double x, final double y, final double width, final double height) {
    final double xOffset = this.nextDouble(width);
    final double yOffset = this.nextDouble(height);

    return new Point2D.Double(x + xOffset, y + yOffset);
  }

  /**
   * Gets a pseudo-random location within the specified boundaries.
   * 
   * @param rect
   *          The rectangle that defines the boundaries.
   * @return A pseudo-random location within the specified bounds.
   */
  public Point2D getLocation(final Rectangle2D rect) {
    return this.getLocation(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
  }

  /**
   * Gets a pseudo-random location within the specified entity boundaries.
   * 
   * @param entity
   *          The entity that defines the boundaries.
   * @return A pseudo-random location within the specified bounds.
   * 
   * @see IEntity#getBoundingBox()
   */
  public Point2D getLocation(final IEntity entity) {
    return this.getLocation(entity.getBoundingBox());
  }

  /**
   * Gets a pseudo-random location within the specified map boundaries.
   * 
   * @param map
   *          The map that defines the boundaries.
   * @return A pseudo-random location within the specified bounds.
   * 
   * @see IMap#getBounds()
   */
  public Point2D getLocation(final IMap map) {
    return this.getLocation(map.getBounds());
  }

  /**
   * Gets a pseudo-random location in the specified circle.
   * 
   * @param circle
   *          The circle that defines the boundaries.
   * @return A pseudo-random location on the specified circle.
   */
  public Point2D getLocation(final Ellipse2D circle) {
    double radius = circle.getWidth() / 2.0;

    double a = Math.random() * 2 * Math.PI;
    double r = radius * Math.sqrt(Math.random());
    double x = r * Math.cos(a) + radius;
    double y = r * Math.sin(a) + radius;

    return new Point2D.Double(circle.getX() + x, circle.getY() + y);
  }

  /**
   * Gets a pseudo-random location on the specified line.
   * 
   * @param line
   *          The line that defines the boundaries.
   * @return A pseudo-random location on the specified line.
   * 
   * @see Line2D#getP1()
   * @see Line2D#getP2()
   */
  public Point2D getLocation(final Line2D line) {
    return this.getLocation(line.getP1(), line.getP2());
  }

  /**
   * Gets a pseudo-random location on the line connecting the two specified points.
   * 
   * @param start
   *          The start point.
   * @param end
   *          The end point.
   * @return A pseudo-random location on the line connecting the two specified points.
   */
  public Point2D getLocation(final Point2D start, final Point2D end) {
    double rnd = this.nextDouble(start.distance(end));
    return GeometricUtilities.project(start, end, rnd);
  }

  /**
   * Gets a pseudo-random char value.
   * 
   * @return A pseudo-random character.
   */
  public char nextChar() {
    char start = '\u0000';
    char end = '\uFFFF';
    return (char) (start + this.nextDouble() * (end - start + 1));
  }

  /**
   * Gets a pseudo-random char value from the specified alphabet.
   * 
   * @param alphabet
   *          The alphabet to chose the character from.
   * @return A pseudo-random character from the specified alphabet.
   */
  public char nextChar(final String alphabet) {
    return alphabet.charAt(this.nextInt(alphabet.length()));
  }

  /**
   * Gets a pseudo-random char value.
   * 
   * @return A pseudo-random character.
   */
  public char nextAscii() {
    return (char) (32 + this.nextDouble() * (126 - 32 + 1));
  }

  /**
   * Gets a pseudo-random String of the specified length.
   * 
   * <p>
   * Characters will be chosen from the set of characters whose ASCII value is between 32 and 126 (inclusive)
   * </p>
   * 
   * @param length
   *          The length of the String.
   * @return A pseudo-random ASCII String.
   */
  public String nextAscii(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(this.nextAscii());
    }

    return sb.toString();
  }

  /**
   * Gets a pseudo-random alphanumeric String of the specified length.
   * 
   * @param length
   *          The length of the String.
   * @return A pseudo-random alphanumeric String.
   */
  public String nextAlphanumeric(final int length) {
    return this.nextAlphanumeric(length, false);
  }

  /**
   * Gets a pseudo-random alphanumeric String of the specified length.
   * 
   * @param length
   *          The length of the String.
   * @param lowerCase
   *          Indicates whether lower-case letters will be included in the String.
   * @return A pseudo-random alphanumeric String.
   */
  public String nextAlphanumeric(final int length, final boolean lowerCase) {
    return this.nextAlphanumeric(length, true, lowerCase);
  }

  /**
   * Gets a pseudo-random alphanumeric String of the specified length.
   * 
   * @param length
   *          The length of the String.
   * @param digit
   *          Indicates whether digits will be included in the string.
   * @param lowerCase
   *          Indicates whether lower-case letters will be included in the String.
   * @return A pseudo-random alphanumeric String.
   */
  public String nextAlphanumeric(final int length, final boolean digit, final boolean lowerCase) {
    final String digits = "0123456789";
    final String upperLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    String alphabet = upperLetters;

    if (lowerCase) {
      alphabet += upperLetters.toLowerCase();
    }

    if (digit) {
      alphabet += digits;
    }

    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(this.nextChar(alphabet));
    }

    return sb.toString();
  }

  /**
   * Gets a pseudo-random alphabetic String of the specified length.
   * 
   * @param length
   *          The length of the String.
   * @return A pseudo-random alphabetic String.
   */
  public String nextAlphabetic(final int length) {
    return this.nextAlphanumeric(length, false, false);
  }

  /**
   * Gets a pseudo-random alphabetic String of the specified length.
   * 
   * @param length
   *          The length of the String.
   * @param lowerCase
   *          Indicates whether lower-case letters will be included in the String.
   * @return A pseudo-random alphabetic String.
   */
  public String nextAlphabetic(final int length, final boolean lowerCase) {
    return this.nextAlphanumeric(length, false, lowerCase);
  }
}
