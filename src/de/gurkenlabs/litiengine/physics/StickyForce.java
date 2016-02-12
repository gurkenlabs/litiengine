/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * The Force implementation sticks to an entity in terms of its location.
 */
public class StickyForce extends Force {

  /** The force entiy. */
  private final IEntity forceEntiy;

  /**
   * Instantiates a new sticky force.
   *
   * @param affectedEntity
   *          the affected entity
   * @param forceEntity
   *          the force entity
   * @param strength
   *          the strength in pixels per second
   * @param duration
   *          the duration
   * @param cancelOnCollision
   *          the cancel on collision
   */
  public StickyForce(final IEntity forceEntity, final float strength, final float size) {
    super(forceEntity.getDimensionCenter(), strength, size);
    this.forceEntiy = forceEntity;
  }

  /**
   * Gets the force entiy.
   *
   * @return the force entiy
   */
  public IEntity getForceEntiy() {
    return this.forceEntiy;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.physics.Force#getLocation()
   */
  @Override
  public Point2D getLocation() {
    return this.getForceEntiy().getDimensionCenter();
  }
}
