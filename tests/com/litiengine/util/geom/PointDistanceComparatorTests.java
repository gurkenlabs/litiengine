package com.litiengine.util.geom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.awt.geom.Point2D;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class PointDistanceComparatorTests {

  @Test
  public void testPointDistanceComparator() {

    final Point2D[] points = new Point2D[4];

    final Point2D point1 = new Point2D.Double(1, 1);
    final Point2D point2 = new Point2D.Double(2, 2);
    final Point2D point3 = new Point2D.Double(3, 3);
    final Point2D point4 = new Point2D.Double(4, 4);
    points[0] = point1;
    points[1] = point2;
    points[2] = point3;
    points[3] = point4;

    final Point2D relativePoint = new Point2D.Double(0, 0);
    Arrays.sort(points, new PointDistanceComparator(relativePoint));
    assertArrayEquals(new Point2D[] { point1, point2, point3, point4 }, points);

    final Point2D relativePoint2 = new Point2D.Double(5, 5);
    Arrays.sort(points, new PointDistanceComparator(relativePoint2));
    assertArrayEquals(new Point2D[] { point4, point3, point2, point1 }, points);

    final Point2D relativePoint3 = new Point2D.Double(2.4, 2.4);
    Arrays.sort(points, new PointDistanceComparator(relativePoint3));
    assertArrayEquals(new Point2D[] { point2, point3, point1, point4 }, points);
  }
}
