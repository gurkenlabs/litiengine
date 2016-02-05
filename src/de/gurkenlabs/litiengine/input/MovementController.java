package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.entities.IMovableEntity;

public class MovementController implements IMovementController {

  private final IMovableEntity movableEntity;

  public MovementController(final IMovableEntity movableEntity) {
    this.movableEntity = movableEntity;
  }

  @Override
  public IMovableEntity getControlledEntity() {
    return this.movableEntity;
  }
}
