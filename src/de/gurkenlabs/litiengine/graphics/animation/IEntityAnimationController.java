package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.entities.IEntity;

public interface IEntityAnimationController<T extends IEntity> extends IAnimationController {
  public T getEntity();
}
