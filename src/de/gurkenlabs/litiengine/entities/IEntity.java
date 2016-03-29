package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;

public interface IEntity {
  public IAnimationController getAnimationController();

  public Rectangle2D getBoundingBox();

  public Point2D getDimensionCenter();

  public float getHeight();

  public Point2D getLocation();

  /**
   * Sets the map location.
   *
   * @param location
   *          the new map location
   */
  public void setLocation(Point2D location);

  public int getMapId();

  public float getWidth();

  public float getAngle();

  public void setAnimationController(IAnimationController animationController);

  /**
   * Sets an id which should only be filled when an entity gets added due to map
   * information.
   *
   * @param mapId
   */
  public void setMapId(int mapId);

  public void setSize(float width, float height);
}
