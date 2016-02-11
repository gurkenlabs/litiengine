package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.physics.Force;

public interface IMovementController {
  public IMovableEntity getControlledEntity();
  public void apply(Force force);
}
