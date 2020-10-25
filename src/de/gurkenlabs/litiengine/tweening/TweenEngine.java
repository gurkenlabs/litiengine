package de.gurkenlabs.litiengine.tweening;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ILaunchable;
import de.gurkenlabs.litiengine.IUpdateable;

public class TweenEngine implements IUpdateable, ILaunchable {
  public Tween startTween(Tweenable target, TweenType type, long duration) {

    Tween tween = this.getTween(target, type);
    if (tween == null) {
      tween = new Tween(target, type, duration).ease(TweenFunction.QUAD_INOUT);
      this.getTweens().add(tween);
    } else {
      tween.setDuration(duration);
      tween.start();
    }
    return tween;
  }

  public Tween stopTween(Tweenable target, TweenType type) {
    Tween tween = this.getTween(target, type);
    if (tween != null) {
      tween.stop();
    }
    return tween;
  }

  private List<Tween> tweens;

  public TweenEngine() {
    this.tweens = new CopyOnWriteArrayList<>();
  }

  public List<Tween> getTweens() {
    return this.tweens;
  }

  public Tween getTween(Tweenable tweenable, TweenType type) {
    Optional<Tween> filter = this.getTweens().stream().filter(c -> c.getTarget() == tweenable && c.getType() == type).findFirst();
    return filter.isPresent() ? filter.get() : null;
  }

  public void register(Tween tween) {
    if (this.getTweens().contains(tween)) {
      return;
    }
    this.getTweens().add(tween);
  }

  @Override
  public void update() {
    for (Tween tween : this.getTweens()) {
      if (tween.hasStopped()) {
        continue;
      }
      long elapsed = Game.time().since(tween.getStartTime());
      if (elapsed >= tween.getDuration()) {
        tween.stop();
        this.getTweens().remove(tween);
        continue;
      }
      float[] currentValues = new float[tween.getTargetValues().length];
      for (int i = 0; i < tween.getTargetValues().length; i++) {
        currentValues[i] = tween.getStartValues()[i] + tween.getEquation().compute(elapsed / (float) tween.getDuration()) * (tween.getTargetValues()[i] - tween.getStartValues()[i]);
      }
      tween.getTarget().setTweenValues(tween.getType(), currentValues);
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
