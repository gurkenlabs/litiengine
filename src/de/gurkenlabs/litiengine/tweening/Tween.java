package de.gurkenlabs.litiengine.tweening;

import java.util.Arrays;

import de.gurkenlabs.litiengine.Game;

/**
 * A Tween is an interpolation between start values and target values over a given time period. It modifies the start values by applying an easing
 * function ({@code TweenEquation}) each tick.
 */
public class Tween {
  private long duration;
  private TweenEquation equation;
  private long started;
  private final float[] startValues;
  private boolean stopped;
  private final Tweenable target;
  private float[] targetValues;
  private final TweenType type;

  /**
   * Instantiates a new tween.
   *
   * @param target
   *          the {@code Tweenable} target object
   * @param type
   *          the {@code TweenType} determining which values of the target object will be modified.
   * @param duration
   *          the duration of the Tween in milliseconds.
   */
  public Tween(final Tweenable target, final TweenType type, final long duration) {
    this.target = target;
    this.type = type;
    this.duration = duration;
    this.startValues = this.target.getTweenValues(type);
    this.targetValues = new float[this.startValues.length];
  }

  /**
   * Begins the Tween by setting its start time to the current game time in ticks.
   *
   * @return the Tween instance
   */
  public Tween begin() {
    this.started = Game.time().now();
    this.stopped = false;
    return this;
  }

  /**
   * Sets a custom easing function for this Tween.
   *
   * @param easeEquation
   *          the {@code TweenEquation} applied to the tween values.
   * @return the Tween instance.
   */
  public Tween ease(final TweenEquation easeEquation) {
    this.equation = easeEquation;
    return this;
  }

  /**
   * Sets a predefined easing function for this Tween.
   *
   * @param easingFunction
   *          the {@code TweenFunction} applied to the tween values.
   * @return the Tween instance.
   */
  public Tween ease(final TweenFunction easingFunction) {
    this.equation = easingFunction.getEquation();
    return this;
  }

  /**
   * Gets the duration of the Tween.
   *
   * @return the duration of the Tween in milliseconds
   */
  public long getDuration() {
    return this.duration;
  }

  /**
   * Gets the tween equation that modifies the start values each tick.
   *
   * @return the TweenEquation
   */
  public TweenEquation getEquation() {
    return this.equation;
  }

  /**
   * Gets the start time of the Tween.
   *
   * @return the start time in milliseconds
   */
  public long getStartTime() {
    return this.started;
  }

  /**
   * Gets the start values.
   *
   * @return the start values
   */
  public float[] getStartValues() {
    return this.startValues;
  }

  /**
   * Gets the {@code Tweenable} target object.
   *
   * @return the target
   */
  public Tweenable getTarget() {
    return this.target;
  }

  /**
   * Gets the target values.
   *
   * @return the target values
   */
  public float[] getTargetValues() {
    return this.targetValues;
  }

  /**
   * Gets the tween type determining which values of the {@code Tweenable} object will be modified.
   *
   * @return the {@code TweenType} of this Tween
   */
  public TweenType getType() {
    return this.type;
  }

  /**
   * Checks if the Tween has stopped.
   *
   * @return true, if successful
   */
  public boolean hasStopped() {
    return this.stopped;
  }

  /**
   * Resets the Tween values to the start values.
   *
   * @return the Tween instance
   */
  public Tween reset() {
    this.getTarget().setTweenValues(this.getType(), this.startValues);
    return this;
  }

  /**
   * Resumes the stopped Tween.
   *
   * @return the Tween instance
   */
  public Tween resume() {
    this.stopped = false;
    return this;
  }

  /**
   * Sets the Tween duration.
   *
   * @param duration
   *          the new duration in milliseconds
   */
  public void setDuration(final long duration) {
    this.duration = duration;
  }

  /**
   * Stops the Tween.
   *
   * @return the Tween instance
   */
  public Tween stop() {
    this.stopped = true;
    return this;
  }

  /**
   * Sets the target values absolutely.
   *
   * @param targetValues
   *          the absolute target values
   * @return the Tween instance
   */
  public Tween target(final float... targetValues) {
    if (this.getStartValues().length == 0 || targetValues.length == 0) {
      return this;
    }
    this.targetValues = Arrays.copyOf(targetValues, targetValues.length);
    return this;
  }

  /**
   * Sets the target values relatively to the start values.
   *
   * @param targetValues
   *          the relative target values with respect to the start values
   * @return the Tween instance
   */
  public Tween targetRelative(final float... targetValues) {
    if (this.getStartValues().length == 0 || targetValues.length == 0) {
      return this;
    }
    this.targetValues = new float[targetValues.length];
    for (int i = 0; i < targetValues.length; i++) {
      this.targetValues[i] = this.getStartValues()[i] + targetValues[i];
    }
    return this;
  }
}
