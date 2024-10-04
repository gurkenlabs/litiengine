package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Represents a path consisting of a series of points.
 */
public class Path {
  private final Path2D path2D;

  private final List<Point2D> points;
  private final Point2D start;

  private final Point2D target;

  /**
   * Constructs a Path object from a given Path2D object.
   *
   * @param path the Path2D object representing the path.
   */
  public Path(final Path2D path) {
    this.path2D = path;
    this.points = GeometricUtilities.getPoints(this.path2D);
    if (!this.points.isEmpty()) {
      this.start = getPoints().getFirst();
      this.target = getPoints().getLast();
    } else {
      this.start = null;
      this.target = null;
    }
  }

  /**
   * Constructs a Path object with specified start and target points, Path2D object, and list of points.
   *
   * @param start  the starting point of the path.
   * @param target the target point of the path.
   * @param path   the Path2D object representing the path.
   * @param points the list of points that make up the path.
   */
  public Path(final Point2D start, final Point2D target, final Path2D path, final List<Point2D> points) {
    this.start = start;
    this.target = target;
    this.path2D = path;
    this.points = points;
  }

  /**
   * Gets the Path2D object representing the path.
   *
   * @return the Path2D object.
   */
  public Path2D getPath() {
    return path2D;
  }

  /**
   * Gets the list of points that make up the path.
   *
   * @return the list of points.
   */
  public List<Point2D> getPoints() {
    return points;
  }

  /**
   * Gets the starting point of the path.
   *
   * @return the starting point.
   */
  public Point2D getStart() {
    return start;
  }

  /**
   * Gets the target point of the path.
   *
   * @return the target point.
   */
  public Point2D getTarget() {
    return target;
  }
}
