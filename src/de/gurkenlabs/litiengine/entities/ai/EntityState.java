package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.entities.IEntityProvider;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.states.State;

public abstract class EntityState<T extends Entity> extends State implements IEntityProvider {
  private final T entity;
  private final Environment environment;

  protected EntityState(final String name, final T entity, final Environment env) {
    super(name);
    this.entity = entity;
    this.environment = env;
  }

  @Override
  public T getEntity() {
    return this.entity;
  }

  public Environment getEnvironment() {
    return this.environment;
  }
}
