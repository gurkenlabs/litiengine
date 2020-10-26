package de.gurkenlabs.litiengine.tweening;

import java.util.Arrays;

import de.gurkenlabs.litiengine.Game;

public class Tween {
  private TweenType type;
  private TweenEquation equation;
  private long duration;
  private Tweenable target;
  private long started;
  private boolean stopped;
  private float[] startValues;
  private float[] targetValues;

  public Tween(Tweenable target, TweenType type, long duration) {
    this.target = target;
    this.type = type;
    this.duration = duration;
    this.startValues = this.target.getTweenValues(type);
    this.targetValues = new float[this.startValues.length];
  }

  public boolean hasStopped() {
    return this.stopped;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public TweenEquation getEquation() {
    return this.equation;
  }

  public long getDuration() {
    return this.duration;
  }

  public Tweenable getTarget() {
    return this.target;
  }

  public long getStartTime() {
    return this.started;
  }

  public float[] getStartValues() {
    return this.startValues;
  }

  public float[] getTargetValues() {
    return this.targetValues;
  }

  public TweenType getType() {
    return this.type;
  }

  public Tween ease(TweenEquation easeEquation) {
    this.equation = easeEquation;
    return this;
  }

  public Tween ease(TweenFunction easingFunction) {
    this.equation = easingFunction.getEquation();
    return this;
  }

  public Tween target(final float... targetValues) {
    this.targetValues = Arrays.copyOf(targetValues, targetValues.length);
    return this;
  }

  public Tween targetRelative(final float... targetValues) {
    this.targetValues = new float[targetValues.length];
    for (int i = 0; i < targetValues.length; i++) {
      this.targetValues[i] = this.getStartValues()[i] + targetValues[i];
    }
    return this;
  }

  public Tween begin() {
    this.started = Game.time().now();
    this.stopped = false;
    return this;
  }

  public Tween resume() {
    this.stopped = false;
    return this;
  }

  public Tween reset() {
    this.getTarget().setTweenValues(this.getType(), this.startValues);
    return this;
  }

  public Tween stop() {
    this.stopped = true;
    return this;
  }
}
