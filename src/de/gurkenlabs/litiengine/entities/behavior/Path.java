package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

/** The Class Path. */
public class Path {

  /** The path. */
  private final Path2D path2D;

  private final List<Point2D> points;
  private final Point2D start;

  /** The target. */
  private final Point2D target;

  public Path(final Path2D path) {
    this.path2D = path;
    this.points = GeometricUtilities.getPoints(this.path2D);
    if (!this.points.isEmpty()) {
      this.start = this.points.get(0);
      this.target = this.points.get(this.points.size() - 1);
    } else {
      this.start = null;
      this.target = null;
    }
  }

  /***
   * Instantiates a new path.
   *
   * @param start
   *          The starting point of the path
   * @param target
   *          The target of this instance
   * @param path
   *          The {@link Path2D} reference
   * @param points
   *          A list of all {@link Point2D} contained by the path
   */
  public Path(
      final Point2D start, final Point2D target, final Path2D path, final List<Point2D> points) {
    this.start = start;
    this.target = target;
    this.path2D = path;
    this.points = points;
  }

  /**
   * Gets the path.
   *
   * @return the path
   */
  public Path2D getPath() {
    return this.path2D;
  }

  public List<Point2D> getPoints() {
    return this.points;
  }

  public Point2D getStart() {
    return this.start;
  }

  /**
   * Gets the target.
   *
   * @return the target
   */
  public Point2D getTarget() {
    return this.target;
  }
}
