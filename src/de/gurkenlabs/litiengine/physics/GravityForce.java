package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.Direction;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.util.geom.GeometricUtilities;

public class GravityForce extends Force {
  /** The force entiy. */
  private final IEntity forceEntiy;

  private final Direction direction;

  public GravityForce(final IEntity forceEntity, final float strength, final Direction dir) {
    super(forceEntity.getDimensionCenter(), strength, 0);
    this.forceEntiy = forceEntity;
    this.direction = dir;
    this.setCancelOnCollision(false);
    this.setCancelOnReached(false);
  }

  /**
   * Gets the force entiy.
   *
   * @return the force entiy
   */
  public IEntity getForceEntiy() {
    return this.forceEntiy;
  }

  @Override
  public Point2D getLocation() {
    Point2D gravityLocation = GeometricUtilities.project(this.getForceEntiy().getDimensionCenter(), Direction.toAngle(this.direction), Math.max(this.forceEntiy.getHeight(),this.forceEntiy.getWidth() * 2 + this.getStrength())); 
    return gravityLocation;
  }
}
