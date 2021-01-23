package de.gurkenlabs.litiengine.tweening;

/**
 * An interface to generally apply a function to a value.
 */
public interface TweenEquation {

  /**
   * Applies the function to the value and returns the result.
   *
   * @param progress
   *          the current value
   * @return a {@code float} representing the result of applying the {@code TweenEquation} to the value.
   */
  float compute(final float progress);
}
