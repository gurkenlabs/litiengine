package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.states.StateMachine;

public class AIBehavior<T extends Entity> extends StateMachine implements IEntityController<T> {
  private final T entity;

  protected AIBehavior(final T entity) {
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
