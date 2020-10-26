package de.gurkenlabs.litiengine.tweening;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ILaunchable;
import de.gurkenlabs.litiengine.IUpdateable;

public class TweenEngine implements IUpdateable, ILaunchable {
  private Map<Tweenable, Map<TweenType, Tween>> tweens;

  public Tween begin(Tweenable target, TweenType type, long duration) {
    Tween tween = this.getTween(target, type);
    if (tween == null) {
      tween = new Tween(target, type, duration).ease(TweenFunction.QUAD_INOUT);
      this.getTweens().get(target).put(type, tween);
    } else {
      tween.setDuration(duration);
      tween.begin();
    }
    return tween;
  }

  public Tween reset(Tweenable target, TweenType type) {
    Tween tween = this.getTween(target, type);
    if (tween != null) {
      tween.stop();
      tween.reset();
    }
    return tween;
  }

  public Tween resume(Tweenable target, TweenType type) {
    Tween tween = this.getTween(target, type);
    if (tween != null) {
      tween.resume();
    }
    return tween;
  }

  public Tween stop(Tweenable target, TweenType type) {
    Tween tween = this.getTween(target, type);
    if (tween != null) {
      tween.stop();
    }
    return tween;
  }

  public TweenEngine() {
    this.tweens = new ConcurrentHashMap<>();
  }

  public Map<Tweenable, Map<TweenType, Tween>> getTweens() {
    return this.tweens;
  }

  public Tween getTween(Tweenable target, TweenType type) {
    if (this.getTweens().get(target) == null) {
      this.getTweens().put(target, new ConcurrentHashMap<>());
    }

    return this.getTweens().get(target).get(type);
  }

  @Override
  public void update() {
    for (Tweenable target : this.getTweens().keySet()) {
      for (Tween tween : this.getTweens().get(target).values()) {
        if (tween.hasStopped()) {
          continue;
        }
        long elapsed = Game.time().since(tween.getStartTime());
        if (elapsed >= tween.getDuration()) {
          tween.stop();
          continue;
        }
        float[] currentValues = new float[tween.getTargetValues().length];
        for (int i = 0; i < tween.getTargetValues().length; i++) {
          currentValues[i] = tween.getStartValues()[i] + tween.getEquation().compute(elapsed / (float) tween.getDuration()) * (tween.getTargetValues()[i] - tween.getStartValues()[i]);
        }
        tween.getTarget().setTweenValues(tween.getType(), currentValues);
      }
    }
  }

  @Override
  public void start() {
    Game.loop().attach(this);
  }

  @Override
  public void terminate() {
    Game.loop().detach(this);
  }
}
