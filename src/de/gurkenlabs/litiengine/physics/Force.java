/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.util.geom.GeometricUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class Force.
 */
public class Force implements IUpdateable {

  /** The affected entity. */
  private final IMovableEntity affectedEntity;

  /** The cancel on collision. */
  private final boolean cancelOnCollision;

  /** The cancel on force source reached. */
  private final boolean cancelOnForceSourceReached;

  /** The duration. */
  private int duration;

  /** The has ended. */
  private boolean hasEnded;

  /** The location. */
  private final Point2D location;

  /** The start tick. */
  private long aliveTick;

  /** The strength. */
  private final float strength;

  /**
   * Instantiates a new force.
   *
   * @param affectedEntity
   *          the affected entity
   * @param location
   *          the location
   * @param strength
   *          the strength
   * @param duration
   *          the duration
   * @param cancelOnCollision
   *          the cancel on collision
   */
  public Force(final IMovableEntity affectedEntity, final Point2D location, final float strength, final int duration, final boolean cancelOnCollision) {
    this.affectedEntity = affectedEntity;
    this.location = location;
    this.strength = strength;
    this.duration = duration;
    this.cancelOnCollision = cancelOnCollision;
    this.cancelOnForceSourceReached = true;
  }

  /**
   * Apply.
   */
  public void apply() {
    this.aliveTick = Game.getLoop().getTicks();
    Game.getLoop().registerForUpdate(this);
  }

  /**
   * Cancel on collision.
   *
   * @return true, if successful
   */
  public boolean cancelOnCollision() {
    return this.cancelOnCollision;
  }

  /**
   * Gets the affected entity.
   *
   * @return the affected entity
   */
  public IMovableEntity getAffectedEntity() {
    return this.affectedEntity;
  }

  /**
   * Gets the duration.
   *
   * @return the duration
   */
  public int getDuration() {
    return this.duration;
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
   * Gets the strength.
   *
   * @return the strength
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

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.core.IUpdateable#update()
   */
  @Override
  public void update() {
    if (Game.getLoop().getDeltaTime(this.aliveTick) > this.getDuration()) {
      Game.getLoop().unregisterFromUpdate(this);
      this.hasEnded = true;
      return;
    }

    if (this.cancelOnForceSourceReached && this.getAffectedEntity().getCollisionBox().contains(this.getLocation())) {
      this.duration = 0;
      return;
    }

    final double angle = GeometricUtilities.calcRotationAngleInDegrees(this.getAffectedEntity().getDimensionCenter(), this.getLocation());
    final boolean success = Game.getPhysicsEngine().move(this.getAffectedEntity(), angle, this.getStrength());
    if (this.cancelOnCollision() && !success) {
      this.duration = 0;
    }
  }
}
