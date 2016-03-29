/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * The Class Path.
 */
public class Path {

  /** The path. */
  private final Path2D path;

  private final Point2D start;
  /** The target. */
  private final Point2D target;

  private final List<Point2D> points;

  /**
   * Instantiates a new path.
   *
   * @param entity
   *          the entity
   * @param target
   *          the target
   * @param path
   *          the path
   */
  public Path(final Point2D start, final Point2D target, final Path2D path, final List<Point2D> points) {
    this.start = start;
    this.target = target;
    this.path = path;
    this.points = points;
  }

  /**
   * Gets the path.
   *
   * @return the path
   */
  public Path2D getPath() {
    return this.path;
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

  public List<Point2D> getPoints() {
    return this.points;
  }
}
