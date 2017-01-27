package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.ai.IEntityController;

public interface IEntityAnimationController<T extends IEntity> extends IAnimationController {
  public T getEntity();
}
