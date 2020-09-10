package de.gurkenlabs.litiengine.tweening;

public interface Tweenable {
  /**
   * Gets one or many values from the target object associated to the
   * given tween type. It is used by the Tween Engine to determine starting
   * values.
   *
   * @param tweenType The tween typ e of this interpikation, determining which values are modified.
   *
   * @return The array of current tween values.
   */
  float[] getTweenValues(TweenType tweenType);

  /**
   * This method is called in a Tween's update() method to set the new interpolated values.
   *
   * @param tweenType The tween typ e of this interpikation, determining which values are modified.
   * @param newValues The new values determined by the tween equation.
   */
  void setTweenValues(TweenType tweenType, float[] newValues);
}
