package de.gurkenlabs.litiengine.physics;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import java.awt.geom.Point2D;

/**
 * GravityForce is a gravitational force pulling entities towards a given direction or angle.
 */
public class GravityForce extends Force {

  /**
   * The angle at which the gravitational force is applied.
   */
  private final float directionAngle;

  /**
   * The entity on which the gravitational force is acting.
   */
  private final IEntity forceEntity;

  /**
   * Instantiates a new GravityForce.
   *
   * @param forceEntity The entity on which the gravitational force is applied.
   * @param strength    The strength of the gravitational force.
   * @param direction   The direction of the gravitational force.
   */
  public GravityForce(final IEntity forceEntity, final float strength, final Direction direction) {
    this(forceEntity, strength, direction.toAngle());
  }

  /**
   * Instantiates a new GravityForce.
   *
   * @param forceEntity The entity on which the gravitational force is applied.
   * @param strength    The strength of the gravitational force.
   * @param angle       The angle at which the gravitational force is applied.
   */
  public GravityForce(final IEntity forceEntity, final float strength, final float angle) {
    super(forceEntity.getCenter(), strength, 0);
    this.forceEntity = forceEntity;
    this.directionAngle = angle;
    setCancelOnCollision(false);
    setCancelOnReached(false);
  }

  /**
   * Gets the entity on which the gravitational force is applied.
   *
   * @return The force entity.
   */
  public IEntity getForceEntity() {
    return this.forceEntity;
  }

  /**
   * Calculates and returns the location where the gravitational force is applied. This location is
   * calculated by projecting the force onto a point based on the entity's center, angle, and
   * strength.
   *
   * @return The location where the gravitational force is applied.
   */
  @Override
  public Point2D getLocation() {
    return GeometricUtilities.project(
      getForceEntity().getCenter(),
      directionAngle,
      Math.max(getForceEntity().getHeight(), getForceEntity().getWidth() * 2 + getStrength()));
  }
}
