/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.entities;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;

/**
 * The Class Entity.
 */
@EntityInfo
public abstract class Entity implements IEntity {  
  private float height;

  private int mapId;

  /** The map location. */
  private Point2D mapLocation;

  private float width;

  private IAnimationController animationController;

  /**
   * Instantiates a new entity.
   */
  protected Entity() {
    this.mapLocation = new Point(0, 0);

    final EntityInfo info = this.getClass().getAnnotation(EntityInfo.class);
    this.setSize(info.width(), info.height());
  }

  @Override
  public IAnimationController getAnimationController() {
    return this.animationController;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return new Rectangle2D.Double(this.getLocation().getX(), this.getLocation().getY(), this.getWidth(), this.getHeight());
  }

  /**
   * Gets the map dimension center.
   *
   * @return the map dimension center
   */
  @Override
  public Point2D getDimensionCenter() {
    return new Point2D.Double(this.getLocation().getX() + this.getWidth() / 2, this.getLocation().getY() + this.getHeight() / 2);
  }

  @Override
  public float getHeight() {
    return this.height;
  }

  /**
   * Gets the map location.
   *
   * @return the map location
   */
  @Override
  public Point2D getLocation() {
    return this.mapLocation;
  }

  @Override
  public int getMapId() {
    return this.mapId;
  }

  @Override
  public float getWidth() {
    return this.width;
  }

  @Override
  public void setAnimationController(final IAnimationController animationController) {
    this.animationController = animationController;
  }

  /**
   * Sets an id which should only be filled when an entity gets added due to map
   * information.
   *
   * @param mapId
   */
  @Override
  public void setMapId(final int mapId) {
    this.mapId = mapId;
  }

  @Override
  public void setSize(final float width, final float height) {
    this.width = width;
    this.height = height;
  }
  
  /**
   * Sets the map location.
   *
   * @param location
   *          the new map location
   */
  protected void setLocation(final Point2D location) {
    this.mapLocation = location;
  }
}