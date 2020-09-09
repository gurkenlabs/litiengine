package de.gurkenlabs.litiengine.tweening;

public interface Tweenable {
  /**
   * Gets one or many values from the target object associated to the
   * given tween type. It is used by the Tween Engine to determine starting
   * values.
   *
   * @param tweenType An arbitrary number used to associate an interpolation type for a tween in the TweenAccessor get/setValues() methods
   *
   * @return The array of current tween values.
   */
  float[] getValues(TweenType tweenType);

  /**
   * This method is called by the Tween Engine each time a running tween
   * associated with the current target object has been updated.
   *
   * @param tweenType An arbitrary number used to associate an interpolation type for a tween in the TweenAccessor get/setValues() methods
   * @param newValues The new values determined by the Tween Engine.
   */
  void setValues(TweenType tweenType, float[] newValues);
}
