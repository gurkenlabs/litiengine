/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;

/**
 * The Interface IPathFinder.
 */
public interface IPathFinder {

  /**
   * Apply path margin.
   *
   * @param entity
   *          the entity
   * @param rectangle
   *          the rectangle
   * @return the rectangle2 d
   */
  public Rectangle2D applyPathMargin(ICollisionEntity entity, Rectangle2D rectangle);

  /**
   * Gets the path.
   *
   * @param start
   *          the start
   * @param target
   *          the goal
   * @return the path
   */
  public Path findPath(IMovableEntity start, Point2D target);
}
