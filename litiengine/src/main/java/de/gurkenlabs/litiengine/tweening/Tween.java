package de.gurkenlabs.litiengine.tweening;

import de.gurkenlabs.litiengine.Game;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Tween is an interpolation between start values and target values over a given time period. It modifies the start values by applying an easing
 * function ({@code TweenEquation}) each tick.
 */
public class Tween {
  private int duration;
  private TweenEquation equation;
  private long started;
  private final float[] startValues;
  private boolean stopped;
  private final Tweenable target;
  private float[] targetValues;
  private final TweenType type;
  private TweenLoop loop = TweenLoop.NONE;
  private boolean reversed;
  private final Collection<TweenListener> listeners = ConcurrentHashMap.newKeySet();

  /**
   * Instantiates a new tween.
   *
   * @param target   the {@code Tweenable} target object
   * @param type     the {@code TweenType} determining which values of the target object will be modified.
   * @param duration the duration of the Tween in ticks.
   */
  public Tween(final Tweenable target, final TweenType type, final int duration) {
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
    for (final TweenListener listener : this.listeners) {
      listener.started(this);
    }
    return this;
  }

  /**
   * Checks if the Tween is currently running.
   *
   * @return true if the Tween is running, false otherwise.
   */
  public boolean isRunning() {
    return !stopped && Game.time().since(started) < duration;
  }

  /**
   * Sets a custom easing function for this Tween.
   *
   * @param easeEquation the {@code TweenEquation} applied to the tween values.
   * @return the Tween instance.
   */
  public Tween ease(final TweenEquation easeEquation) {
    this.equation = easeEquation;
    return this;
  }

  /**
   * Sets a predefined easing function for this Tween.
   *
   * @param easingFunction the {@code TweenFunction} applied to the tween values.
   * @return the Tween instance.
   */
  public Tween ease(final TweenFunction easingFunction) {
    this.equation = easingFunction.getEquation();
    return this;
  }

  /**
   * Gets the duration of the Tween.
   *
   * @return the duration of the Tween in ticks
   */
  public int getDuration() {
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
   * Gets the start tick of the Tween.
   *
   * @return the start time in ticks
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
   * @param duration the new duration in ticks
   */
  public void setDuration(final int duration) {
    this.duration = duration;
  }

  /**
   * Stops the Tween.
   *
   * @return the Tween instance
   */
  public Tween stop() {
    if (this.stopped) {
      return this;
    }
    this.stopped = true;
    for (final TweenListener listener : this.listeners) {
      listener.stopped(this);
    }
    return this;
  }

  /**
   * Gets the configured loop mode for this Tween.
   *
   * @return the {@code TweenLoop} mode.
   */
  public TweenLoop getLoop() {
    return this.loop;
  }

  /**
   * Sets the loop mode for this Tween. When the Tween completes its duration, it will either stop, restart from the beginning,
   * or reverse direction depending on the configured loop mode.
   *
   * @param loop the desired {@code TweenLoop} mode.
   * @return the Tween instance.
   */
  public Tween loop(final TweenLoop loop) {
    this.loop = loop == null ? TweenLoop.NONE : loop;
    return this;
  }

  /**
   * Checks whether the Tween is currently running in its reversed direction. This is only relevant for {@link TweenLoop#PINGPONG}
   * tweens.
   *
   * @return {@code true} if the Tween currently interpolates from its target values back to its start values.
   */
  public boolean isReversed() {
    return this.reversed;
  }

  /**
   * Toggles the playback direction of the Tween. The next interpolation cycle will swap the role of start and target values.
   *
   * @return the Tween instance.
   */
  Tween toggleReversed() {
    this.reversed = !this.reversed;
    return this;
  }

  /**
   * Registers a {@link TweenListener} that gets notified about the Tween's lifecycle events.
   *
   * @param listener the listener to add.
   * @return the Tween instance.
   */
  public Tween addListener(final TweenListener listener) {
    if (listener != null) {
      this.listeners.add(listener);
    }
    return this;
  }

  /**
   * Removes a previously registered {@link TweenListener}.
   *
   * @param listener the listener to remove.
   * @return the Tween instance.
   */
  public Tween removeListener(final TweenListener listener) {
    this.listeners.remove(listener);
    return this;
  }

  /**
   * Notifies registered listeners that the Tween completed a duration cycle. This is invoked by the {@link TweenEngine} after each
   * cycle, before deciding whether to stop or continue the Tween.
   */
  void notifyCompleted() {
    for (final TweenListener listener : this.listeners) {
      listener.completed(this);
    }
  }

  /**
   * Sets the target values absolutely.
   *
   * @param targetValues the absolute target values
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
   * @param targetValues the relative target values with respect to the start values
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
