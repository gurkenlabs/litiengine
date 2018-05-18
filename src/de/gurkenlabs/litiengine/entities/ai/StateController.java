package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.states.StateMachine;

public class StateController<T extends Entity> extends StateMachine implements IBehaviorController<T> {
  private final T entity;

  protected StateController(final T entity) {
    super();
    this.entity = entity;
  }

  @Override
  public T getEntity() {
    return this.entity;
  }
}
