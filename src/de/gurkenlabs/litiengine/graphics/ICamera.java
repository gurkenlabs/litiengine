/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * The Interface ICamera defines methods that allow to determine where entities
 * or tiles are rendered on the current screen.
 */
public interface ICamera {
  /**
   * Gets the x offset of the camera, which defines the offset that is used to
   * render the focus map location.
   *
   * @return the x offset
   */
  public double getCenterX();

  /**
   * Gets the y offset of the camera, which defines the offset that is used to
   * render the focus map location.
   *
   * @return the x offset
   */
  public double getCenterY();

  /**
   * Gets the map location that is focused by this camera.
   *
   * @return the focus map location
   */
  public Point2D getFocus();

  /**
   * Gets the map location.
   *
   * @param point
   *          the point
   * @return the map location
   */
  public Point2D getMapLocation(Point2D point);

  /**
   * Gets the pixel offset x.
   *
   * @return the pixel offset x
   */
  public double getPixelOffsetX();

  /**
   * Gets the pixel offset y.
   *
   * @return the pixel offset y
   */
  public double getPixelOffsetY();

  /**
   * Gets the camera region.
   *
   * @return the camera region
   */
  public Rectangle2D getViewPort();

  public Point2D getViewPortDimensionCenter(IEntity entity);

  /**
   * Gets the render location.
   *
   * @param x
   *          the x
   * @param y
   *          the y
   * @return the render location
   */
  public Point2D getViewPortLocation(double x, double y);

  /**
   * This method calculates to location for the specified entity in relation to
   * the focus map location of the camera.
   *
   * @param entity
   *          the entity
   * @return the render location
   */
  public Point2D getViewPortLocation(IEntity entity);

  /**
   * This method calculates to location for the specified point in relation to
   * the focus map location of the camera.
   *
   * @param point
   *          the point
   * @return the render location
   */
  public Point2D getViewPortLocation(Point2D point);

  /**
   * Shake the camera by the given offset for the given duration.
   *
   * @param intensity
   *          the intensity
   * @param duration
   *          the duration in update calls.
   */
  public void shake(double intensity, int duration);

  public void updateFocus();
}
