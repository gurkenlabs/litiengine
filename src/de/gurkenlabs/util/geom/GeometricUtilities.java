package de.gurkenlabs.util.geom;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeometricUtilities {

  public static double calcRotationAngleInDegrees(final double centerX, final double centerY, final double targetX, final double targetY) {
    // calculate the angle theta from the deltaY and deltaX values
    // (atan2 returns radians values from [-PI,PI])
    // 0 currently points EAST.
    // NOTE: By preserving Y and X param order to atan2, we are expecting
    // a CLOCKWISE angle direction.
    final double theta = Trigonometry.atan2((float) (targetY - centerY), (float) (targetX - centerX));

    // convert from radians to degrees
    // this will give you an angle from [0->270],[-180,0]
    double angle = Math.toDegrees(theta);

    // rotate by 90 degree in order to match the liti coordinate system
    angle = angle - 90;

    // convert to positive range [0-360)
    // since we want to prevent negative angles, adjust them now.
    // we can assume that atan2 will not return a negative value
    // greater than one partial rotation
    if (angle < 0) {
      angle += 360;
    }

    return 360 - angle;
  }

  /**
   * Calculates the angle from centerPt to targetPt in degrees. The return
   * should range from [0,360), rotating CLOCKWISE, 0 and 360 degrees represents
   * NORTH, 90 degrees represents EAST, etc...
   *
   * Assumes all points are in the same coordinate space. If they are not, you
   * will need to call SwingUtilities.convertPointToScreen or equivalent on all
   * arguments before passing them to this function.
   *
   * @param centerPt
   *          Point we are rotating around.
   * @param targetPt
   *          Point we want to calcuate the angle to.
   * @return angle in degrees. This is the angle from centerPt to targetPt.
   */
  public static double calcRotationAngleInDegrees(final Point2D centerPt, final Point2D targetPt) {
    return calcRotationAngleInDegrees(centerPt.getX(), centerPt.getY(), targetPt.getX(), targetPt.getY());
  }

  /**
   * Contains.
   *
   * @param rectangle
   *          the rectangle
   * @param p
   *          the p
   * @return true, if successful
   */
  public static boolean contains(final Rectangle2D rectangle, final Point2D p) {
    return rectangle.getX() <= p.getX() && rectangle.getY() <= p.getY() && rectangle.getX() + rectangle.getWidth() >= p.getX() && rectangle.getY() + rectangle.getHeight() >= p.getY();
  }

  public static double distance(final double p1X, final double p1Y, final double p2X, final double p2Y) {
    return Math.sqrt((p1X - p2X) * (p1X - p2X) + (p1Y - p2Y) * (p1Y - p2Y));
  }

  public static double distance(final Point2D p1, final Point2D p2) {
    return Math.sqrt((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()));
  }

  /**
   * Distance.
   *
   * @param rect
   *          the rect
   * @param p
   *          the p
   * @return the double
   */
  public static double distance(final Rectangle2D rect, final Point2D p) {
    final double dx = Math.max(rect.getMinX() - p.getX(), p.getX() - rect.getMaxX());
    final double dy = Math.max(rect.getMinY() - p.getY(), p.getY() - rect.getMaxY());
    return Math.sqrt(dx * dx + dy * dy);
  }

  public static boolean equals(final Point2D point1, final Point2D point2, final double epsilon) {
    return point1.distance(point2) < epsilon;
  }

  /**
   * Gets the connecting lines.
   *
   * @param point
   *          the point
   * @param rectangle
   *          the rectangle
   * @return the connecting lines
   */
  public static Line2D[] getConnectingLines(final Point2D point, final Point2D[] rectPoints) {
    final Line2D[] lines = new Line2D[rectPoints.length];

    for (int i = 0; i < rectPoints.length; i++) {
      lines[i] = new Line2D.Double(point.getX(), point.getY(), rectPoints[i].getX(), rectPoints[i].getY());
    }

    return lines;
  }

  public static ArrayList<Line2D.Double> getConstrainingLines(final Area area) {
    final ArrayList<double[]> areaPoints = new ArrayList<>();
    final ArrayList<Line2D.Double> areaSegments = new ArrayList<>();
    final double[] coords = new double[6];

    for (final PathIterator pi = area.getPathIterator(null); !pi.isDone(); pi.next()) {
      // The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
      // Because the Area is composed of straight lines
      final int type = pi.currentSegment(coords);
      // We record a double array of {segment type, x coord, y coord}
      final double[] pathIteratorCoords = { type, coords[0], coords[1] };
      areaPoints.add(pathIteratorCoords);
    }

    double[] start = new double[3]; // To record where each polygon starts

    for (int i = 0; i < areaPoints.size(); i++) {
      // If we're not on the last point, return a line from this point to the
      // next
      final double[] currentElement = areaPoints.get(i);

      // We need a default value in case we've reached the end of the ArrayList
      double[] nextElement = { -1, -1, -1 };
      if (i < areaPoints.size() - 1) {
        nextElement = areaPoints.get(i + 1);
      }

      // Make the lines
      if (currentElement[0] == PathIterator.SEG_MOVETO) {
        start = currentElement; // Record where the polygon started to close it
                                // later
      }

      if (nextElement[0] == PathIterator.SEG_LINETO) {
        areaSegments.add(new Line2D.Double(currentElement[1], currentElement[2], nextElement[1], nextElement[2]));
      } else if (nextElement[0] == PathIterator.SEG_CLOSE) {
        areaSegments.add(new Line2D.Double(currentElement[1], currentElement[2], start[1], start[2]));
      }
    }
    return areaSegments;
  }

  public static float getDeltaX(double angle) {
    angle = angle - 90;

    if (angle < 0) {
      angle += 360;
    }

    angle = 360 - angle;
    return Trigonometry.cosDeg((float) angle);
  }

  public static float getDeltaY(double angle) {
    angle = angle - 90;

    if (angle < 0) {
      angle += 360;
    }

    angle = 360 - angle;
    return Trigonometry.sinDeg((float) angle);
  }

  /**
   * Gets the intersection point.
   *
   * @param lineA
   *          the line a
   * @param lineB
   *          the line b
   * @return the intersection point
   */
  public static Point2D getIntersectionPoint(final Line2D lineA, final Line2D lineB) {

    final double x1 = lineA.getX1();
    final double y1 = lineA.getY1();
    final double x2 = lineA.getX2();
    final double y2 = lineA.getY2();

    final double x3 = lineB.getX1();
    final double y3 = lineB.getY1();
    final double x4 = lineB.getX2();
    final double y4 = lineB.getY2();

    Point2D p = null;

    final double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
    if (d != 0) {
      final double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
      final double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

      if (xi >= Math.min(lineA.getX1(), lineA.getX2()) && xi <= Math.max(lineA.getX1(), lineA.getX2()) && yi >= Math.min(lineA.getY1(), lineA.getY2()) && yi <= Math.max(lineA.getY1(), lineA.getY2())) {
        p = new Point2D.Double(xi, yi);
      }
    }

    return p;
  }

  /**
   * Intersects.
   *
   * @param line
   *          the line
   * @param rectangle
   *          the rectangle
   * @return the point2 d
   */
  public static Point2D getIntersectionPoint(final Line2D line, final Rectangle2D rectangle) {
    final ArrayList<Point2D> intersectionPoints = getIntersectionPoints(line, rectangle);
    for (final Point2D p : intersectionPoints) {
      if (p != null && !p.equals(line.getP1()) && contains(rectangle, p)) {
        return p;
      }
    }
    return null;
  }

  /**
   * Gets the intersection points.
   *
   * @param line
   *          the line
   * @param rectangle
   *          the rectangle
   * @return the intersection points
   */
  public static ArrayList<Point2D> getIntersectionPoints(final Line2D line, final Rectangle2D rectangle) {
    final ArrayList<Point2D> intersectionPoints = new ArrayList<>();
    final Line2D[] lines = getLines(rectangle);
    final Line2D topLine = lines[0];
    final Line2D bottomLine = lines[1];
    final Line2D leftLine = lines[2];
    final Line2D rightLine = lines[3];

    // Top line
    final Point2D p1 = getIntersectionPoint(line, topLine);
    if (p1 != null && contains(rectangle, p1)) {
      intersectionPoints.add(p1);
    }

    // Bottom line
    final Point2D p2 = getIntersectionPoint(line, bottomLine);
    if (p2 != null && contains(rectangle, p2) && !intersectionPoints.contains(p2)) {
      intersectionPoints.add(p2);
    }

    // Left side...
    final Point2D p3 = getIntersectionPoint(line, leftLine);
    if (p3 != null && !p3.equals(p1) && !p3.equals(p2) && contains(rectangle, p3) && !intersectionPoints.contains(p3)) {
      intersectionPoints.add(p3);
    }

    // Right side
    final Point2D p4 = getIntersectionPoint(line, rightLine);
    if (p4 != null && !p4.equals(p1) && !p4.equals(p2) && contains(rectangle, p4) && !intersectionPoints.contains(p4)) {
      intersectionPoints.add(p4);
    }

    intersectionPoints.removeAll(Collections.singleton(null));
    return intersectionPoints;
  }

  /**
   * Gets the lines.
   *
   * @param rectangle
   *          the rectangle
   * @return the lines
   */
  public static Line2D[] getLines(final Rectangle2D rectangle) {
    final Line2D[] lines = new Line2D[4];
    lines[0] = new Line2D.Double(rectangle.getMinX(), rectangle.getMinY(), rectangle.getMinX(), rectangle.getMaxY());
    lines[1] = new Line2D.Double(rectangle.getMinX(), rectangle.getMaxY(), rectangle.getMaxX(), rectangle.getMaxY());
    lines[2] = new Line2D.Double(rectangle.getMaxX(), rectangle.getMaxY(), rectangle.getMaxX(), rectangle.getMinY());
    lines[3] = new Line2D.Double(rectangle.getMaxX(), rectangle.getMinY(), rectangle.getMinX(), rectangle.getMinY());
    return lines;
  }

  public static Point2D getMidPoint(final Point2D p1, final Point2D p2) {
    final Point2D mid = new Point2D.Double();
    final double x = (p1.getX() + p2.getX()) / 2;
    final double y = (p1.getY() + p2.getY()) / 2;
    mid.setLocation(x, y);
    return mid;

  }

  /**
   * Gets the perpendicular intersection.
   *
   * @param point
   *          the point
   * @param line
   *          the line
   * @return the perpendicular intersection
   */
  public static Point2D getPerpendicularIntersection(final Point2D point, final Line2D line) {
    final double x1 = line.getX1();
    final double y1 = line.getY1();
    final double x2 = line.getX2();
    final double y2 = line.getY2();

    final double x3 = point.getX();
    final double y3 = point.getY();

    final double k = ((y2 - y1) * (x3 - x1) - (x2 - x1) * (y3 - y1)) / (Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
    final double x4 = x3 - k * (y2 - y1);
    final double y4 = y3 + k * (x2 - x1);

    return new Point2D.Double(x4, y4);
  }

  public static Point2D getPointOnCircle(final Point2D center, final double radius, final double angle) {
    final double x = center.getX() + radius * Math.cos(Math.toRadians(angle));
    final double y = center.getY() + radius * Math.sin(Math.toRadians(angle));

    return new Point2D.Double(x, y);
  }

  public static List<Point2D> getPoints(final Path2D path) {
    final PathIterator pi = path.getPathIterator(null);
    final double[] coordinates = new double[22];
    final List<Point2D> points = new ArrayList<>();
    while (!pi.isDone()) {
      pi.next();

      pi.currentSegment(coordinates);
      final Point2D currentPoint = new Point2D.Double(coordinates[0], coordinates[1]);
      points.add(currentPoint);
    }

    return points;
  }

  /**
   * Gets the points.
   *
   * @param rectangle
   *          the rectangle
   * @return the points
   */
  public static ArrayList<Point2D> getPoints(final Rectangle2D rectangle) {

    final ArrayList<Point2D> points = new ArrayList<>();
    points.add(new Point2D.Double(rectangle.getMinX(), rectangle.getMinY()));
    points.add(new Point2D.Double(rectangle.getMaxX(), rectangle.getMinY()));
    points.add(new Point2D.Double(rectangle.getMaxX(), rectangle.getMaxY()));
    points.add(new Point2D.Double(rectangle.getMinX(), rectangle.getMaxY()));
    return points;
  }

  /**
   * Gets the points between the specified points using the Bresenham algorithm.
   *
   * @param point1
   *          the point1
   * @param point2
   *          the point2
   * @return the points between points
   */
  public static List<Point2D> getPointsBetweenPoints(final Point2D point1, final Point2D point2) {
    double x0 = point1.getX();
    double y0 = point1.getY();
    final double x1 = point2.getX();
    final double y1 = point2.getY();
    final List<Point2D> line = new ArrayList<>();

    final int dx = (int) Math.abs(x1 - x0);
    final int dy = (int) Math.abs(y1 - y0);

    final int sx = x0 < x1 ? 1 : -1;
    final int sy = y0 < y1 ? 1 : -1;

    int err = dx - dy;
    int e2;

    while (true) {
      line.add(new Point2D.Double(x0, y0));

      if (Math.abs(x0 - x1) < 1 && Math.abs(y0 - y1) < 1) {
        break;
      }

      e2 = 2 * err;
      if (e2 > -dy) {
        err = err - dy;
        x0 = x0 + sx;
      }

      if (e2 < dx) {
        err = err + dx;
        y0 = y0 + sy;
      }
    }
    return line;
  }

  public static Point2D getRandomLocation(final double x, final double y, final double width, final double height) {
    final double xOffset = Math.random() * width;
    final double yOffset = Math.random() * height;

    return new Point2D.Double(x + xOffset, y + yOffset);
  }

  public static Point2D getRandomLocation(final Rectangle2D rect) {
    return getRandomLocation(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());

  }

  public static boolean intersects(final Rectangle2D a, final Rectangle2D b) {
    if (Math.abs(a.getCenterX() - b.getCenterX()) < a.getWidth() * 0.5 + b.getWidth() * 0.5) {
      if (Math.abs(a.getCenterY() - b.getCenterY()) < a.getHeight() * 0.5 + b.getHeight() * 0.5) {
        return true;
      }
    }

    return false;
  }

  /**
   * Project.
   *
   * @param start
   *          the start
   * @param angle
   *          the angle
   * @param delta
   *          the delta
   * @return the point2 d
   */
  public static Point2D project(final Point2D start, final double angle, final double delta) {
    double x = start.getX();
    double y = start.getY();

    // calculate delta
    final double xDelta = getXDelta(angle, delta);
    final double yDelta = getYDelta(angle, delta);
    x += xDelta;
    y += yDelta;

    return new Point2D.Double(x, y);
  }

  /**
   * Projects a point from end along the vector (end - start) by the given
   * scalar amount.
   *
   * @param start
   *          the start
   * @param end
   *          the end
   * @param scalar
   *          the scalar
   * @return the point2 d. double
   */
  public static Point2D project(final Point2D start, final Point2D end, final double scalar) {
    double dx = end.getX() - start.getX();
    double dy = end.getY() - start.getY();

    // euclidean length
    final float len = (float) Math.sqrt(dx * dx + dy * dy);
    // normalize to unit vector
    if (len != 0) { // avoid division by 0
      dx /= len;
      dy /= len;
    }
    // multiply by scalar amount
    dx *= scalar;
    dy *= scalar;
    return new Point2D.Double(start.getX() + dx, start.getY() + dy);
  }

  public static Point2D[] rayCastPoints(final Point2D point, final Rectangle2D rectangle) {
    final double EPSILON = 0.01;
    // 1. get all rectangle points
    final ArrayList<Point2D> rectPoints = getPoints(rectangle);
    rectPoints.sort(new PointDistanceComparator(point));

    // 2. connect point with all rectangle points
    final Line2D[] connectingLines = getConnectingLines(point, rectPoints.toArray(new Point2D[rectPoints.size()]));
    final ArrayList<Point2D> resultPoints = new ArrayList<>();

    for (int i = 0; i < rectPoints.size(); i++) {
      final ArrayList<Point2D> intersectionPoints = getIntersectionPoints(connectingLines[i], rectangle);
      // If there is any intersection point which is not a corner point of the
      // rectangle the rectangle point at index i is not visible because the
      // raycast needs to pass the rectangle first.
      // Thus, the rectangle point at index i will not be added to the result.
      if (intersectionPoints.stream().anyMatch(intersectionPoint -> rectPoints.stream().noneMatch(rectPoint -> equals(rectPoint, intersectionPoint, EPSILON)))) {
        continue;
      }

      resultPoints.add(rectPoints.get(i));
    }

    resultPoints.removeAll(Collections.singleton(null));
    return resultPoints.toArray(new Point2D[resultPoints.size()]);
  }

  public static Shape scaleRect(final Rectangle2D shape, final int max) {
    final double width = shape.getWidth();
    final double height = shape.getHeight();

    if (width == 0 || height == 0) {
      return null;
    }
    double dWidth = 0;
    double dHeight = 0;
    final double ratio = width / height;
    final double newHeight = width / ratio;
    final double newWidth = height * ratio;

    if (newWidth == newHeight) {
      dWidth = max;
      dHeight = max;
    } else if (newWidth > newHeight) {
      dWidth = max;
      dHeight = height / width * max;
    } else {
      dHeight = max;
      dWidth = width / height * max;
    }
    final AffineTransform transform = AffineTransform.getScaleInstance(dWidth, dHeight);
    return transform.createTransformedShape(shape);
  }

  public static Shape scaleShape(final Shape shape, final double scale) {
    final AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);
    return transform.createTransformedShape(shape);
  }

  /**
   * Shape intersects. WARNING: USE THIS METHOD WITH CAUTION BECAUSE IT IS A
   * VERY SLOW WAY OF CALCULATING INTERSECTIONS.
   *
   * @param shapeA
   *          the shape a
   * @param shapeB
   *          the shape b
   * @return true, if successful
   */
  public static boolean shapeIntersects(final Shape shapeA, final Shape shapeB) {
    final Area areaA = new Area(shapeA);
    areaA.intersect(new Area(shapeB));
    return !areaA.isEmpty();
  }

  public static Shape translateShape(final Shape shape, final Point2D renderLocation) {
    final AffineTransform t = new AffineTransform();
    t.translate(renderLocation.getX(), renderLocation.getY());
    return shape;

  }

  private static double getXDelta(final double angle, final double delta) {
    return Trigonometry.sin((float) Math.toRadians(angle)) * delta * 100 / 100.0;
  }

  private static double getYDelta(final double angle, final double delta) {
    return Trigonometry.cos((float) Math.toRadians(angle)) * delta * 100 / 100.0;
  }
}
