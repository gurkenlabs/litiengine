/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.entities;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.RenderType;

/**
 * The Class Entity.
 */
@EntityInfo
public abstract class Entity implements IEntity {
  private float height;

  private int mapId;

  private String name;

  /** The map location. */
  private Point2D mapLocation;

  /** The direction. */
  private float angle;

  private float width;

  private RenderType renderType;

  private Rectangle2D boundingBox;

  /**
   * Instantiates a new entity.
   */
  protected Entity() {
    this.mapLocation = new Point(0, 0);
    final EntityInfo info = this.getClass().getAnnotation(EntityInfo.class);
    this.width = info.width();
    this.height = info.height();
    this.renderType = info.renderType();
    if (Game.getEnvironment() != null) {
      Game.getEnvironment().getEntities().add(this);
    }
  }

  @Override
  public float getAngle() {
    return this.angle;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    if (boundingBox != null) {
      return this.boundingBox;
    }

    return this.boundingBox = new Rectangle2D.Double(this.getLocation().getX(), this.getLocation().getY(), this.getWidth(), this.getHeight());
  }

  /**
   * Gets the map dimension center.
   *
   * @return the map dimension center
   */
  @Override
  public Point2D getDimensionCenter() {
    return new Point2D.Double(this.getLocation().getX() + this.getWidth() * 0.5, this.getLocation().getY() + this.getHeight() * 0.5);
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
  public String sendMessage(Object sender, final String message) {
    return null;
  }

  protected void setAngle(final float angle) {
    this.angle = angle;
  }

  /**
   * Sets the map location.
   *
   * @param location
   *          the new map location
   */
  @Override
  public void setLocation(final Point2D location) {
    this.mapLocation = location;
    this.boundingBox = null;
  }

  @Override
  public void setLocation(double x, double y) {
    this.setLocation(new Point2D.Double(x, y));
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
    this.setWidth(width);
    this.setHeight(height);
  }
  
  @Override
  public void setRenderType(RenderType renderType) {
    this.renderType = renderType;
  }

  @Override
  public void setHeight(float height) {
    this.height = height;
    this.boundingBox = null;
  }

  @Override
  public void setWidth(float width) {
    this.width = width;
    this.boundingBox = null;
  }

  public RenderType getRenderType() {
    return renderType;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }
}