package de.gurkenlabs.litiengine.physics.pathfinding;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import de.gurkenlabs.util.geom.GeometricUtilities;
import de.gurkenlabs.util.geom.PointDistanceComparator;

public class PathFindingTests {

  @Test
  public void testGetIntersectionPoints() {

    final Rectangle2D rect = new Rectangle2D.Double(1, 1, 3, 2);

    final Line2D line = new Line2D.Double(0, 0, 4, 4);
    final ArrayList<Point2D> points = GeometricUtilities.getIntersectionPoints(line, rect);
    final Point2D expected1 = new Point2D.Double(1, 1);
    final Point2D expected2 = new Point2D.Double(3, 3);
    Assert.assertArrayEquals(new Point2D[] { expected1, expected2 }, points.toArray());

    final Line2D line2 = new Line2D.Double(1, 1, 1, 4);
    final ArrayList<Point2D> points2 = GeometricUtilities.getIntersectionPoints(line2, rect);
    final Point2D expected3 = new Point2D.Double(1, 1);
    final Point2D expected4 = new Point2D.Double(1, 3);
    Assert.assertArrayEquals(new Point2D[] { expected3, expected4 }, points2.toArray());

    final Line2D line3 = new Line2D.Double(0, 0, 1, 1);
    final ArrayList<Point2D> points3 = GeometricUtilities.getIntersectionPoints(line3, rect);
    final Point2D expected5 = new Point2D.Double(1, 1);
    Assert.assertArrayEquals(new Point2D[] { expected5 }, points3.toArray());

    final Line2D line4 = new Line2D.Double(0, 0, 2, 2);
    final ArrayList<Point2D> points4 = GeometricUtilities.getIntersectionPoints(line4, rect);
    final Point2D expected6 = new Point2D.Double(1, 1);
    Assert.assertArrayEquals(new Point2D[] { expected6 }, points4.toArray());
  }

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
    Assert.assertArrayEquals(new Point2D[] { point1, point2, point3, point4 }, points);

    final Point2D relativePoint2 = new Point2D.Double(5, 5);
    Arrays.sort(points, new PointDistanceComparator(relativePoint2));
    Assert.assertArrayEquals(new Point2D[] { point4, point3, point2, point1 }, points);

    final Point2D relativePoint3 = new Point2D.Double(2.4, 2.4);
    Arrays.sort(points, new PointDistanceComparator(relativePoint3));
    Assert.assertArrayEquals(new Point2D[] { point2, point3, point1, point4 }, points);
  }

  @Test
  public void testRaycast() {

    final Rectangle2D rect = new Rectangle2D.Double(1, 1, 3, 2);

    final Point2D relativePoint = new Point2D.Double(0, 0);
    final Point2D[] possiblePoints = GeometricUtilities.rayCastPoints(relativePoint, rect);
    final Point2D expected1 = new Point2D.Double(1, 1);
    final Point2D expected2 = new Point2D.Double(1, 3);
    final Point2D expected3 = new Point2D.Double(4, 1);
    Assert.assertArrayEquals(new Point2D[] { expected1, expected2, expected3 }, possiblePoints);

    final Point2D relativePoint2 = new Point2D.Double(2, 0);
    final Point2D[] possiblePoint2 = GeometricUtilities.rayCastPoints(relativePoint2, rect);
    final Point2D expected4 = new Point2D.Double(1, 1);
    final Point2D expected5 = new Point2D.Double(4, 1);
    Assert.assertArrayEquals(new Point2D[] { expected4, expected5 }, possiblePoint2);
  }
}
