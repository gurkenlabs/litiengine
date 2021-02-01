package com.litiengine.entities.behavior;

import com.litiengine.entities.Entity;

public abstract class EntityTransition<T extends Entity> extends Transition {
  private final T entity;

  protected EntityTransition(final T entity, final int priority) {
    super(priority);
    this.entity = entity;
  }

  public T getEntity() {
    return this.entity;
  }
}
