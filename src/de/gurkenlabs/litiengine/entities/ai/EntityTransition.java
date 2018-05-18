package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.entities.IEntityProvider;
import de.gurkenlabs.litiengine.states.Transition;

public abstract class EntityTransition<T extends Entity> extends Transition implements IEntityProvider {
  private final T entity;

  protected EntityTransition(final T entity, final int priority) {
    super(priority);
    this.entity = entity;
  }

  public T getEntity() {
    return this.entity;
  }
}
