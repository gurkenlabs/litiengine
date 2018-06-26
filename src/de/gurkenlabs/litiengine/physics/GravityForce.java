package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

public class GravityForce extends Force {
  private final Direction direction;

  /** The force entiy. */
  private final IEntity forceEntity;

  public GravityForce(final IEntity forceEntity, final float strength, final Direction dir) {
    super(forceEntity.getCenter(), strength, 0);
    this.forceEntity = forceEntity;
    this.direction = dir;
    this.setCancelOnCollision(false);
    this.setCancelOnReached(false);
  }

  /**
   * Gets the force entity.
   *
   * @return the force entity
   */
  public IEntity getForceEntity() {
    return this.forceEntity;
  }

  @Override
  public Point2D getLocation() {
    return GeometricUtilities.project(this.getForceEntity().getCenter(), Direction.toAngle(this.direction), Math.max(this.forceEntity.getHeight(), this.forceEntity.getWidth() * 2 + this.getStrength()));
  }
}
