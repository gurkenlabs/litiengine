package com.litiengine.util.geom;

import java.awt.geom.Point2D;
import java.util.Comparator;

/**
 * The Class PointDistanceComparator order points by their distance to the
 * relative point.
 */
public class PointDistanceComparator implements Comparator<Point2D> {

  private final Point2D relativePoint;

  public PointDistanceComparator(final Point2D relativePoint) {
    this.relativePoint = relativePoint;
  }

  @Override
  public int compare(final Point2D point1, final Point2D point2) {
    final double distance1 = point1.distance(this.relativePoint);
    final double distance2 = point2.distance(this.relativePoint);
    if (distance1 < distance2) {
      return -1;
    }
    if (distance1 > distance2) {
      return 1;
    }

    return 0;
  }
}
