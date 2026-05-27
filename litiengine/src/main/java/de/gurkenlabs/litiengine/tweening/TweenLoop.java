package de.gurkenlabs.litiengine.tweening;

/**
 * Determines how a {@code Tween} behaves once its duration has elapsed.
 */
public enum TweenLoop {
  /**
   * The Tween stops after completing once.
   */
  NONE,

  /**
   * The Tween restarts from the beginning after completing.
   */
  LOOP,

  /**
   * The Tween reverses its direction on each completion, swapping start and target values back and forth.
   */
  PINGPONG
}
