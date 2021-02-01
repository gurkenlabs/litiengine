package com.litiengine.graphics.animation;

import java.util.function.Function;
import java.util.function.Predicate;

import com.litiengine.entities.IEntity;
import com.litiengine.entities.IEntityController;

public interface IEntityAnimationController<T extends IEntity> extends IAnimationController, IEntityController {
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
  public void addRule(Predicate<? super T> rule, Function<? super T, String> animationName);

  /**
   * Registers an animation rule that will be evaluated if there is currently no
   * animation playing that is defined to loop. This allows to specify
   * animations that should be applied under certain conditions.
   * 
   * @param rule
   *          The rule that must be fulfilled for the animation to be applied
   * 
   * @param animationName
   *          The callback that evaluates the actual animation name that will be
   *          applied
   * 
   * @param priority
   *          The priority that defines the order in which the rule will be processed. Rules with higher priorities
   *          will be processed first.
   */
  public void addRule(Predicate<? super T> rule, Function<? super T, String> animationName, int priority);

  /**
   * Gets a flag indicating whether this controller instance is auto scaling its animations by the dimensions of the entity.
   * 
   * @return True if this instance is automatically scaling to the dimensions of the entity; otherwise false.
   */
  public boolean isAutoScaling();

  /**
   * Sets a value indicating whether this controller instance is auto scaling its animations by the dimensions of the entity
   * 
   * @param scaling
   *          True if this instance is automatically scaling to the dimensions of the entity; otherwise false.
   */
  public void setAutoScaling(boolean scaling);

  /**
   * Sets the dimensions used to scale the animations of this controller instance.
   * 
   * @param ratioX
   *          The x-ratio to scale the animation with.
   * @param ratioY
   *          The y-ratio to scale the animation with.
   */
  public void scaleSprite(float ratioX, float ratioY);

  /**
   * Sets the ratio used to scale the animations of this controller instance.
   * 
   * @param ratio
   *          The ratio to scale the animation with.
   */
  public void scaleSprite(float ratio);
}
