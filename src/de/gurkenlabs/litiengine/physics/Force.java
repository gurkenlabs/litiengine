/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.ICollisionEntity;

/**
 * The Class Force.
 */
public class Force {
  /** The cancel on collision. */
  private boolean cancelOnCollision;

  /** The cancel on force source reached. */
  private boolean cancelOnReached;

  /** The has ended. */
  private boolean hasEnded;

  /** The location. */
  private final Point2D location;

  /** The strength. */
  private final float strength;

  private final float size;

  /**
   * Instantiates a new force.
   *
   * @param affectedEntity
   *          the affected entity
   * @param location
   *          the location
   * @param strength
   *          the strength in pixels per second
   * @param duration
   *          the duration
   * @param cancelOnCollision
   *          the cancel on collision
   */
  public Force(final Point2D location, final float strength, final float size) {
    this.location = location;
    this.strength = strength;
    this.size = size;
    this.cancelOnCollision = true;
    this.cancelOnReached = true;
  }

  /**
   * Cancel on collision.
   *
   * @return true, if successful
   */
  public boolean cancelOnCollision() {
    return this.cancelOnCollision;
  }

  public boolean cancelOnReached() {
    return this.cancelOnReached;
  }

  public boolean hasReached(final ICollisionEntity entity) {
    return new Ellipse2D.Double(this.getLocation().getX() - this.size / 2, this.getLocation().getY() - this.size / 2, this.size, this.size).intersects(entity.getCollisionBox());
  }

  /**
   * Gets the location.
   *
   * @return the location
   */
  public Point2D getLocation() {
    return this.location;
  }

  /**
   * Gets the strength in pixels per second.
   *
   * @return the strength in pixels per seconds
   */
  public float getStrength() {
    return this.strength;
  }

  /**
   * Checks for ended.
   *
   * @return true, if successful
   */
  public boolean hasEnded() {
    return this.hasEnded;
  }

  public void end() {
    this.hasEnded = true;
  }

  public void setCancelOnCollision(final boolean cancelOnCollision) {
    this.cancelOnCollision = cancelOnCollision;
  }

  public void setCancelOnReached(final boolean cancelOnReached) {
    this.cancelOnReached = cancelOnReached;
  }
}
