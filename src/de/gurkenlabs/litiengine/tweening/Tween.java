package de.gurkenlabs.litiengine.tweening;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;

public class Tween implements IUpdateable {
  public static Tween to(Tweenable target, TweenType type, long duration) {
    return new Tween(target, type, duration).ease(TweenEquations.Quad_InOut);
  }

  private TweenType type;
  private TweenEquation equation;
  private long duration;
  private Tweenable target;
  private long started;
  private float[] startValues;
  private float[] targetValues;

  public Tween(Tweenable target, TweenType type, long duration) {
    this.target = target;
    this.type = type;
    this.duration = duration;
    this.startValues = this.target.getValues(type);
    this.targetValues = new float[this.startValues.length];
  }

  public Tween ease(TweenEquation easeEquation) {
    this.equation = easeEquation;
    return this;
  }

  public Tween ease(TweenEquations easeEquation) {
    this.equation = easeEquation.getEquation();
    return this;
  }

  public Tween target(final float... targetValues) {
    for (int i = 0; i < targetValues.length; i++) {
      this.targetValues[i] = targetValues[i];
    }
    return this;
  }

  public Tween start() {
    Game.loop().attach(this);
    this.started = Game.time().now();
    return this;
  }

  public Tween end() {
    Game.loop().detach(this);
    return this;
  }

  @Override
  public void update() {
    long time = Game.time().now();
    long elapsed = Game.time().since(this.started);
    if (elapsed >= this.duration) {
      this.end();
      return;
    }
    float[] currentValues = new float[this.targetValues.length];
    for (int i = 0; i < this.targetValues.length; i++) {
      currentValues[i] = this.startValues[i] + this.equation.compute(elapsed / (float) (this.duration)) * this.targetValues[i];
    }
    this.target.setValues(this.type, currentValues);
  }

}
