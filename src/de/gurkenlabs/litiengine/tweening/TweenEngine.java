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
  private final Map<Tweenable, Map<TweenType, Tween>> tweens;

  /**
   * Instantiates a new {@code TweenEngine}.
   */
  public TweenEngine() {
    this.tweens = new ConcurrentHashMap<>();
  }

  /**
   * Begins a new Tween. If a Tween is already registered for the {@code Tweenable} with the given {@code TweenType}, it is restarted with the given
   * duration.
   *
   * @param target   the {@code Tweenable} target object
   * @param type     the {@code TweenType} determining which values of the target object will be modified.
   * @param duration the duration of the Tween in milliseconds.
   * @return the Tween instance
   */
  public Tween begin(final Tweenable target, final TweenType type, final long duration) {
    Tween tween = this.getTween(target, type);
    if (tween == null) {
      tween = new Tween(target, type, duration).ease(TweenFunction.QUAD_INOUT);
      this.getTweens().get(target).put(type, tween);
    } else {
      tween.setDuration(duration);
    }
    tween.begin();
    return tween;
  }

  /**
   * Attempts to get a previously registered {@code Tween} or registers a new one.
   *
   * @param target the {@code Tweenable} target object
   * @param type   the {@code TweenType} determining which values of the target object will be modified.
   * @return the Tween instance
   */
  public Tween getTween(final Tweenable target, final TweenType type) {
    if (this.getTweens().get(target) == null) {
      this.getTweens().put(target, new ConcurrentHashMap<>());
    }

    return this.getTweens().get(target).get(type);
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
   * Looks for a registered Tween instance with the given target and type. Attempts to stop the Tween and reset the {@code Tweenable} values to the
   * start values.
   *
   * @param target the {@code Tweenable} target object
   * @param type   the {@code TweenType} determining which values of the target object will be modified.
   * @return the Tween instance
   */
  public Tween reset(final Tweenable target, final TweenType type) {
    final Tween tween = this.getTween(target, type);
    if (tween != null) {
      tween.stop();
      tween.reset();
    }
    return tween;
  }

  /**
   * Looks for a registered Tween instance with the given target and type. Attempts to resume the Tween if it was stopped.
   *
   * @param target the {@code Tweenable} target object
   * @param type   the {@code TweenType} determining which values of the target object will be modified.
   * @return the Tween instance
   */
  public Tween resume(final Tweenable target, final TweenType type) {
    final Tween tween = this.getTween(target, type);
    if (tween != null) {
      tween.resume();
    }
    return tween;
  }

  /**
   * Start.
   */
  @Override
  public void start() {
    Game.loop().attach(this);
  }

  /**
   * Looks for a registered Tween instance with the given target and type. Attempts to stop the Tween.
   *
   * @param target the {@code Tweenable} target object
   * @param type   the {@code TweenType} determining which values of the target object will be modified.
   * @return the Tween instance
   */
  public Tween stop(final Tweenable target, final TweenType type) {
    final Tween tween = this.getTween(target, type);
    if (tween != null) {
      tween.stop();
    }
    return tween;
  }

  /**
   * Terminate.
   */
  @Override
  public void terminate() {
    Game.loop().detach(this);
  }

  /**
   * Updates all registered Tweens by applying the {@code TweenEquation}.
   */
  @Override
  public void update() {
    for (final Tweenable target : this.getTweens().keySet()) {
      for (final Tween tween : this.getTweens().get(target).values()) {
        if (tween.hasStopped()) {
          continue;
        }
        final long elapsed = Game.time().since(tween.getStartTime());
        if (elapsed >= tween.getDuration()) {
          tween.stop();
          continue;
        }
        final float[] currentValues = new float[tween.getTargetValues().length];
        for (int i = 0; i < tween.getTargetValues().length; i++) {
          currentValues[i] =
              tween.getStartValues()[i] + tween.getEquation().compute(elapsed / (float) tween.getDuration()) * (tween.getTargetValues()[i] - tween
                  .getStartValues()[i]);
        }
        tween.getTarget().setTweenValues(tween.getType(), currentValues);
      }
    }
  }
}
