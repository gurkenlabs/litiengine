package de.gurkenlabs.litiengine.graphics.animation;

import java.util.function.Function;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.entities.IEntityController;

public interface IEntityAnimationController<T> extends IAnimationController, IEntityController {
  /**
   * Registers an animation rule that will be evaluated if there is currently no
   * animation playing that is defined to loop. This allows to specify
   * animations that should be applied under certain conditions.
   * 
   * @param rule
   *          The rule that must be fulfilled for the animation to be applied
   * @param animationName
   *          The callback that evaluates the actual animation name that will be
   *          applied
   */
  public void addAnimationRule(Predicate<? super T> rule, Function<? super T, ? extends String> animationName);

  public void addAnimationRule(Predicate<? super T> rule, Function<? super T, ? extends String> animationName, int priority);

  public boolean isAutoScaling();

  public void setAutoScaling(boolean scaling);

  public void scaleSprite(float scaleX, float scaleY);

  public void scaleSprite(float scale);
}
