package de.gurkenlabs.litiengine.physics;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import java.awt.geom.Point2D;

public class GravityForce extends Force {
  private final float directionAngle;

  /** The force entity. */
  private final IEntity forceEntity;

  public GravityForce(final IEntity forceEntity, final float strength, final Direction direction) {
    this(forceEntity, strength, direction.toAngle());
  }

  public GravityForce(final IEntity forceEntity, final float strength, final float angle) {
    super(forceEntity.getCenter(), strength, 0);
    this.forceEntity = forceEntity;
    this.directionAngle = angle;
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
    return GeometricUtilities.project(
        getForceEntity().getCenter(),
        directionAngle,
        Math.max(getForceEntity().getHeight(), getForceEntity().getWidth() * 2 + getStrength()));
  }
}
