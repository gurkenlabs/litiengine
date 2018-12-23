package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.entities.IMobileEntity;

/**
 * A movement controller that supports keyboard input for horizontal entity movement.
 *
 * @param <T>
 *          The type of the controlled entity.
 */
public class PlatformingMovementController<T extends IMobileEntity> extends KeyboardEntityController<T> {

  public PlatformingMovementController(T entity) {
    super(entity);
    this.getUpKeys().clear();
    this.getDownKeys().clear();
  }
}