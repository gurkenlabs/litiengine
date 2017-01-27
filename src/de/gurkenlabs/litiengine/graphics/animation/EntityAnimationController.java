package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.ai.IEntityController;

public class EntityAnimationController<T extends IEntity> extends AnimationController implements IEntityAnimationController<T> {
  private final T entity;

  public EntityAnimationController(T entity, Animation defaultAnimation, Animation[] animations) {
    super(defaultAnimation, animations);
    this.entity = entity;
  }

  public T getEntity() {
    return this.entity;
  }
}
