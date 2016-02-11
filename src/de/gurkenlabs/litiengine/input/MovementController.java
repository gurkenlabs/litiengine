package de.gurkenlabs.litiengine.input;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.physics.Force;

public class MovementController implements IUpdateable, IMovementController {
  private final List<Force> activeForces;
  private final IMovableEntity movableEntity;

  public MovementController(final IMovableEntity movableEntity) {
    this.activeForces = new CopyOnWriteArrayList<>();
    this.movableEntity = movableEntity;
    Game.getLoop().registerForUpdate(this);
  }

  @Override
  public IMovableEntity getControlledEntity() {
    return this.movableEntity;
  }

  @Override
  public void apply(Force force) {
    if (!this.activeForces.contains(force)) {
      this.activeForces.add(force);
    }
  }

  @Override
  public void update() {
    // clean up forces
    this.activeForces.forEach(x -> {
      if (x.hasEnded()) {
        this.activeForces.remove(x);
      }
    });
  }

  protected List<Force> getActiceForces() {
    return this.activeForces;
  }
}
