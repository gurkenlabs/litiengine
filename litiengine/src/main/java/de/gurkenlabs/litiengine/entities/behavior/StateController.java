package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * Controls the state of an entity.
 *
 * @param <T> the type of entity
 */
public class StateController<T extends IEntity> extends StateMachine
  implements IBehaviorController {
  private final T entity;

  /**
   * Initializes a new instance of the StateController class with the specified entity.
   *
   * @param entity the entity associated with this state controller
   */
  protected StateController(final T entity) {
    super();
    this.entity = entity;
  }

  /**
   * Gets the entity associated with this state controller.
   *
   * @return the entity associated with this state controller
   */
  @Override
  public T getEntity() {
    return this.entity;
  }
}
