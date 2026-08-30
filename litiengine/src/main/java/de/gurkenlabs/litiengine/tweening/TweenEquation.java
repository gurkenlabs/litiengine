package de.gurkenlabs.litiengine.tweening;

/// An interface to generally apply a function to a value.
public interface TweenEquation {

  /// Applies the function to the value and returns the result.
  ///
  /// @param progress
  /// the current value
  /// @return a `float` representing the result of applying the `TweenEquation` to the value.
  float compute(final float progress);
}
