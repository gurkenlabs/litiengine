package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IMovableEntity;

public class ClientEntityMovementController extends EntityMovementController{

  public ClientEntityMovementController(IMovableEntity movableEntity) {
    super(Game.getLoop(), Game.getPhysicsEngine(), movableEntity);
  }
}
