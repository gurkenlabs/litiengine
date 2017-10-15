package de.gurkenlabs.litiengine.graphics.animation;

import java.util.function.Predicate;

import de.gurkenlabs.litiengine.entities.IEntity;

public interface IEntityAnimationController<T extends IEntity> extends IAnimationController {
  public T getEntity();

  public void addAnimationRule(Predicate<T> rule, String animationName);
}
