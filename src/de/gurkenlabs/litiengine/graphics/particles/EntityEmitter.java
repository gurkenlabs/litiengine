/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * An abstract implementation for emitters that are bound to
 * {@link de.gurkenlabs.litiengine.entities.IEntity#getLocation()}.
 */
public abstract class EntityEmitter extends Emitter {

  /** The entity. */
  private final IEntity entity;

  /**
   * Instantiates a new entity emitter.
   *
   * @param entity
   *          the entity
   */
  public EntityEmitter(final IEntity entity) {
    super((int) entity.getDimensionCenter().getX(), (int) entity.getDimensionCenter().getY());
    this.entity = entity;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.entities.Entity#getCollisionBox()
   */
  @Override
  public Rectangle2D getBoundingBox() {
    return this.getEntity().getBoundingBox();
  }

  /**
   * Gets the entity.
   *
   * @return the entity
   */
  public IEntity getEntity() {
    return this.entity;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.entities.Entity#getMapLocation()
   */
  @Override
  public Point2D getLocation() {
    return this.getEntity().getDimensionCenter();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.entities.Entity#setMapLocation(java.awt.geom.Point2D)
   */
  @Override
  public void setLocation(final Point2D location) {
  }
}
