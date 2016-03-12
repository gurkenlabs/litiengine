package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.tiled.tmx.IEnvironment;
import de.gurkenlabs.util.states.State;

public abstract class EntityState<T extends Entity> extends State implements IEntityState<T> {
  private final T entity;
  private final IEnvironment environment;

  protected EntityState(String name, T entity, IEnvironment env) {
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
