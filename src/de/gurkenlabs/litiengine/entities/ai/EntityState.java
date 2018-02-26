package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.states.State;

public abstract class EntityState<T extends Entity> extends State implements IEntityState<T> {
  private final T entity;
  private final IEnvironment environment;

  protected EntityState(final String name, final T entity, final IEnvironment env) {
    super(name);
    this.entity = entity;
    this.environment = env;
  }

  @Override
  public T getEntity() {
    return this.entity;
  }

  public IEnvironment getEnvironment() {
    return this.environment;
  }
}
