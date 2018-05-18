package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.states.StateMachine;

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

  @Override
  public void detach() {
    Game.getLoop().detach(this);
  }

  @Override
  public void attach() {
    Game.getLoop().attach(this);
  }
}
