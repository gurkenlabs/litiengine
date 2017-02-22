package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.entities.IEntity;

public class EntityAnimationController<T extends IEntity> extends AnimationController implements IEntityAnimationController<T> {
  private final T entity;

  public EntityAnimationController(final T entity, final Animation defaultAnimation, final Animation[] animations) {
    super(defaultAnimation, animations);
    this.entity = entity;
  }

  @Override
  public T getEntity() {
    return this.entity;
  }

  @Override
  public void playAnimation(String animationName) {
    // TODO Auto-generated method stub
    super.playAnimation(animationName);
  }
  
}
