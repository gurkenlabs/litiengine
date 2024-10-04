package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * Represents a transition for an entity.
 *
 * @param <T> the type of entity
 */
public abstract class EntityTransition<T extends IEntity> extends Transition {

  private final T entity;

  /**
   * Initializes a new instance of the EntityTransition class with the specified entity, priority, and state.
   *
   * @param entity   the entity associated with this transition
   * @param priority the priority of the transition
   * @param state    the state of the transition
   */
  protected EntityTransition(final T entity, final int priority, final State state) {
    super(priority, state);
    this.entity = entity;
  }

  /**
   * Initializes a new instance of the EntityTransition class with the specified entity and priority.
   *
   * @param entity   the entity associated with this transition
   * @param priority the priority of the transition
   */
  protected EntityTransition(final T entity, final int priority) {
    super(priority);
    this.entity = entity;
  }

  /**
   * Gets the entity associated with this transition.
   *
   * @return the entity associated with this transition
   */
  public T getEntity() {
    return this.entity;
  }
}
