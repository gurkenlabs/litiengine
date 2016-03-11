package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;

public interface IEntityController<T extends IEntity> extends IUpdateable {

  public T getEntity();
}
