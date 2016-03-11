package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.util.states.State;

public abstract class EntityState<T extends Entity> extends State implements IEntityState<T> {
  private final T entity;

  protected EntityState(String name, T entity) {
    super(name);
    this.entity = entity;
  }

  @Override
  public T getEntity() {
    return this.entity;
  }
}
