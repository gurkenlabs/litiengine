package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.environment.Environment;

public abstract class EntityState<T extends Entity> extends State {
  private final T entity;
  private final Environment environment;

  protected EntityState(final String name, final T entity, final Environment env) {
    super(name);
    this.entity = entity;
    this.environment = env;
  }

  public T getEntity() {
    return this.entity;
  }

  public Environment getEnvironment() {
    return this.environment;
  }
}
