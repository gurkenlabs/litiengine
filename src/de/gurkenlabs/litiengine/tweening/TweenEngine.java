package de.gurkenlabs.litiengine.tweening;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ILaunchable;
import de.gurkenlabs.litiengine.IUpdateable;

/**
 * The TweenEngine is the central manager for Tweens. It tracks all current Tween instances and applies their {@code TweenEquation} with every
 * {@code GameLoop} update.
 */
public class TweenEngine implements IUpdateable, ILaunchable {
  private Map<Tweenable, Map<TweenType, Tween>> tweens;

  /**
   * Begins a new Tween. If a Tween is already registered for the {@code Tweenable} with the given {@code TweenType}, it is restarted with the given
   * duration.
   *
   * @param target
   *          the {@code Tweenable} target object
   * @param type
   *          the {@code TweenType} determining which values of the target object will be modified.
   * @param duration
   *          the duration of the Tween in milliseconds.
   * @return the Tween instance
   */
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

  /**
   * Looks for a registered Tween instance with the given target and type. Attempts to stop the Tween and reset the {@code Tweenable} values to the
   * start values.
   *
   * @param target
   *          the {@code Tweenable} target object
   * @param type
   *          the {@code TweenType} determining which values of the target object will be modified.
   * @return the Tween instance
   */
  public Tween reset(Tweenable target, TweenType type) {
    Tween tween = this.getTween(target, type);
    if (tween != null) {
      tween.stop();
      tween.reset();
    }
    return tween;
  }

  /**
   * Looks for a registered Tween instance with the given target and type. Attempts to resume the Tween if it was stopped.
   *
   * @param target
   *          the {@code Tweenable} target object
   * @param type
   *          the {@code TweenType} determining which values of the target object will be modified.
   * @return the Tween instance
   */
  public Tween resume(Tweenable target, TweenType type) {
    Tween tween = this.getTween(target, type);
    if (tween != null) {
      tween.resume();
    }
    return tween;
  }

  /**
   * Looks for a registered Tween instance with the given target and type. Attempts to stop the Tween.
   *
   * @param target
   *          the {@code Tweenable} target object
   * @param type
   *          the {@code TweenType} determining which values of the target object will be modified.
   * @return the Tween instance
   */
  public Tween stop(Tweenable target, TweenType type) {
    Tween tween = this.getTween(target, type);
    if (tween != null) {
      tween.stop();
    }
    return tween;
  }

  /**
   * Instantiates a new {@code TweenEngine}.
   */
  public TweenEngine() {
    this.tweens = new ConcurrentHashMap<>();
  }

  /**
   * Gets the map of registered {@code Tweens}.
   *
   * @return the map of registered {@code Tweens}.
   */
  public Map<Tweenable, Map<TweenType, Tween>> getTweens() {
    return this.tweens;
  }

  /**
   * Attempts to get a previously registered {@code Tween} or registers a new one.
   *
   * @param target
   *          the {@code Tweenable} target object
   * @param type
   *          the {@code TweenType} determining which values of the target object will be modified.
   * @return the Tween instance
   */
  public Tween getTween(Tweenable target, TweenType type) {
    if (this.getTweens().get(target) == null) {
      this.getTweens().put(target, new ConcurrentHashMap<>());
    }

    return this.getTweens().get(target).get(type);
  }

  /**
   * Updates all registered Tweens by applying the {@code TweenEquation}.
   */
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

  /**
   * Start.
   */
  @Override
  public void start() {
    Game.loop().attach(this);
  }

  /**
   * Terminate.
   */
  @Override
  public void terminate() {
    Game.loop().detach(this);
  }
}
