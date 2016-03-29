package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.util.states.StateMachine;

public class EntityController<T extends Entity> extends StateMachine implements IEntityController<T> {
  private final T entity;

  protected EntityController(final IGameLoop loop, final T entity) {
    super(loop);
    this.entity = entity;
  }

  @Override
  public void update(final IGameLoop loop) {
    super.update(loop);
  }

  @Override
  public T getEntity() {
    return this.entity;
  }
}
