package com.litiengine.physics;

import java.awt.geom.Point2D;

import com.litiengine.entities.IEntity;

/**
 * The Force implementation sticks to an entity in terms of its location.
 */
public class StickyForce extends Force {

  /** The force entiy. */
  private final IEntity forceEntiy;

  /***
   * Instantiates a new sticky force.
   * 
   * @param forceEntity
   *          The entity to who's location this force will be bound
   * @param strength
   *          The strength/intensity of this force
   * @param size
   *          The size of this force
   */
  public StickyForce(final IEntity forceEntity, final float strength, final float size) {
    super(forceEntity.getCenter(), strength, size);
    this.forceEntiy = forceEntity;
  }

  public StickyForce(final Point2D center, final float strength, final float size) {
    super(center, strength, size);
    this.forceEntiy = null;
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
    if (this.getForceEntiy() != null) {
      return this.getForceEntiy().getCenter();
    }
    return super.getLocation();
  }
}
