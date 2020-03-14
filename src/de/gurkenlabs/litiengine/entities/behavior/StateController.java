package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.entities.IEntity;

public class StateController<T extends IEntity> extends StateMachine implements IBehaviorController {
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
