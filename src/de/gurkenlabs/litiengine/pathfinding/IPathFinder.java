package de.gurkenlabs.litiengine.pathfinding;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.IMobileEntity;

/**
 * The Interface IPathFinder.
 */
public interface IPathFinder {

  /**
   * Gets the path.
   *
   * @param start
   *          the start
   * @param target
   *          the goal
   * @return the path
   */
  public Path findPath(IMobileEntity start, Point2D target);
}
