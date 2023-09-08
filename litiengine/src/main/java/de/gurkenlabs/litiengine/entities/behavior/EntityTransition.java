package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.entities.IEntity;

public abstract class EntityTransition<T extends IEntity> extends Transition {

  private final T entity;

  protected EntityTransition(final T entity, final int priority, final State state) {
    super(priority, state);
    this.entity = entity;
  }

  protected EntityTransition(final T entity, final int priority) {
    super(priority);
    this.entity = entity;
  }

  public T getEntity() {
    return this.entity;
  }
}
