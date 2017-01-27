package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.physics.MovementController;

public class ClientEntityMovementController<T extends IMovableEntity> extends MovementController<T> {

  public ClientEntityMovementController(final T movableEntity) {
    super(Game.getPhysicsEngine(), movableEntity);
  }
}
