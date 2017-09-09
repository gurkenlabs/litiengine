package de.gurkenlabs.litiengine.physics.pathfinding;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.physics.Path;

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
  public Path findPath(IMovableEntity start, Point2D target);
}
