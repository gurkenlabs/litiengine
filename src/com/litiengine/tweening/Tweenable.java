package com.litiengine.tweening;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Tweenable interface allows modifying an object's attributes smoothly over time using {@code Tween} instances managed by the
 * {@code TweenEngine}.
 */
public interface Tweenable {

  /**
   * Gets one or many values from the target object associated to the
   * given tween type. It is used by the Tween Engine to determine starting
   * values.
   *
   * @param tweenType
   *          The tween type of this interpolation, determining which values are modified.
   *
   * @return The array of current tween values.
   */
  default float[] getTweenValues(final TweenType tweenType) {
    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, () -> String.format("TweenType %s unsupported for Tweenable '%s'", tweenType.name(), this.getClass().getName()));
    return new float[0];
  }

  /**
   * This method is called in a Tween's update() method to set the new interpolated values.
   *
   * @param tweenType
   *          The tween type of this interpolation, determining which values are modified.
   * @param newValues
   *          The new values determined by the tween equation.
   */
  default void setTweenValues(final TweenType tweenType, final float[] newValues) {
    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, () -> String.format("TweenType %s unsupported for Tweenable '%s'", tweenType.name(), this.getClass().getName()));
  }
}
