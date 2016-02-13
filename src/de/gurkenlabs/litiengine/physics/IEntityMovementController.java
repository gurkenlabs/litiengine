package de.gurkenlabs.litiengine.physics;

import java.util.List;

import de.gurkenlabs.litiengine.entities.IMovableEntity;

public interface IEntityMovementController {
  public IMovableEntity getControlledEntity();
  public void apply(Force force);
  public List<Force> getActiceForces();
}
