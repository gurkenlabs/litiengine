package de.gurkenlabs.litiengine.input;

import java.util.List;

import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.physics.Force;

public interface IEntityMovementController {
  public IMovableEntity getControlledEntity();
  public void apply(Force force);
  public List<Force> getActiceForces();
}
