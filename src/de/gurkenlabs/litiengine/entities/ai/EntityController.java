package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.util.states.StateMachine;

public class EntityController<T extends Entity> extends StateMachine implements IEntityController<T> {
  private final T entity;

  protected EntityController(final T entity) {
    super();
    this.entity = entity;
  }

  @Override
  public T getEntity() {
    return this.entity;
  }

  @Override
  public void update(final IGameLoop loop) {
    super.update(loop);
  }
}
