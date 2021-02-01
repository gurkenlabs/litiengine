package com.litiengine.physics;

import java.awt.geom.Point2D;

import com.litiengine.entities.IEntity;
import com.litiengine.Direction;
import com.litiengine.util.geom.GeometricUtilities;

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
    return GeometricUtilities.project(this.getForceEntity().getCenter(), this.directionAngle, Math.max(this.forceEntity.getHeight(), this.forceEntity.getWidth() * 2 + this.getStrength()));
  }

}
