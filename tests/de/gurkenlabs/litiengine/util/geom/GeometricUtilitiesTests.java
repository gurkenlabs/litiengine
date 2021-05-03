package de.gurkenlabs.litiengine.util.geom;

import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Dimension2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeometricUtilitiesTests {

  @Test
  public void testScaleWithRatio() {
    int width1 = 16;
    int height1 = 32;

    int width2 = 32;
    int height2 = 16;

    Dimension2D newDimension1 = GeometricUtilities.scaleWithRatio(width1, height1, 16);
    Dimension2D newDimension2 = GeometricUtilities.scaleWithRatio(width2, height2, 16);

    assertEquals(8, newDimension1.getWidth(), 0.0001);
    assertEquals(16, newDimension1.getHeight(), 0.0001);

    assertEquals(16, newDimension2.getWidth(), 0.0001);
    assertEquals(8, newDimension2.getHeight(), 0.0001);
  }

  @ParameterizedTest(name="testCalcRotationAngleInDegrees x={0}, y={1}, expectedAngle={2}")
  @CsvSource({
    "1.0d, 1.0d, 45.0f",
    "1.0d, 0, 90.0d",
    "0, 1.0d, 0",
    "-1.0d, -1.0d, 225.0f",
    "-1.0d, 0, 270.0f",
    "0, -1.0d, 180.0f",
    "1.0d, -1.0d, 135.0f",
    "-1.0d, 1.0d, 315.0f"
  })
  public void testCalcRotationAngleInDegrees(double x, double y, float expectedAngle){
    // arrange
    Point2D.Double centerPoint = new Point2D.Double(0, 0);
    Point2D.Double targetPoint = new Point2D.Double(x, y);

    // act
    double rotationAngle = GeometricUtilities.calcRotationAngleInDegrees(centerPoint, targetPoint);

    // assert
    assertEquals(expectedAngle, (float)rotationAngle);
  }

  @Test
  public void testGetMidPoint() {
    Point2D mid = GeometricUtilities.getCenter(new Point2D.Double(0, 0), new Point2D.Double(0, 1));
    Point2D mid2 = GeometricUtilities.getCenter(new Line2D.Double(new Point2D.Double(0,0), new Point2D.Double(0,1)));
    Point2D mid3 = GeometricUtilities.getCenter(0, 0, 1, 1);
    Point2D mid4 = GeometricUtilities.getCenter(GeometricUtilities.getCircle(new Point2D.Double(0.5d, 0.5d), 0.5d));
    Point2D mid5 = GeometricUtilities.getCenter(new Ellipse2D.Double(0, 0, 1, 1));
    Point2D mid6 = GeometricUtilities.getCenter(new Rectangle2D.Double(0, 0, 1, 1));
    Point2D mid7 = GeometricUtilities.getCenter(new Arc2D.Double(0, 0, 1, 1, 1, 1, Arc2D.OPEN));
    
    Rectangle2D rectangle8 = new Rectangle2D.Double(5, 5, 10, 10);
    Point2D mid8 = GeometricUtilities.getCenter(rectangle8);
    
    Rectangle2D rectangle9 = new Rectangle2D.Double(-5, -5, 10, 10);
    Point2D mid9 = GeometricUtilities.getCenter(rectangle9);
    
    assertEquals(new Point2D.Double(0, 0.5), mid);
    assertEquals(new Point2D.Double(0, 0.5), mid2);
    assertEquals(new Point2D.Double(0.5, 0.5), mid3);
    assertEquals(new Point2D.Double(0.5, 0.5), mid4);
    assertEquals(new Point2D.Double(0.5, 0.5), mid5);
    assertEquals(new Point2D.Double(0.5, 0.5), mid6);
    assertEquals(new Point2D.Double(0.5, 0.5), mid7);
    assertEquals(new Point2D.Double(10, 10), mid8);
    assertEquals(new Point2D.Double(0, 0), mid9);
  }

  @Test
  public void testRaycast() {
    final Rectangle2D rect = new Rectangle2D.Double(1, 1, 3, 2);

    final Point2D relativePoint = new Point2D.Double(0, 0);
    final Point2D[] possiblePoints = GeometricUtilities.rayCastPoints(relativePoint, rect);
    final Point2D expected1 = new Point2D.Double(1, 1);
    final Point2D expected2 = new Point2D.Double(1, 3);
    final Point2D expected3 = new Point2D.Double(4, 1);
    assertArrayEquals(new Point2D[] { expected1, expected2, expected3 }, possiblePoints);

    final Point2D relativePoint2 = new Point2D.Double(2, 0);
    final Point2D[] possiblePoint2 = GeometricUtilities.rayCastPoints(relativePoint2, rect);
    final Point2D expected4 = new Point2D.Double(1, 1);
    final Point2D expected5 = new Point2D.Double(4, 1);
    assertArrayEquals(new Point2D[] { expected4, expected5 }, possiblePoint2);
  }
  
  @ParameterizedTest(name="testProjectByAngle_xCoordinate angle={0}, expectedX={1}")
  @CsvSource({
          "90.0d, 1.0d",
          "180.0d, 0",
          "270.0d, -1.0d",
          "360, 0"
  })
  public void testProjectionByAngle_xCoordinate(double angle, double expectedX){
    // arrange
    final Point2D start = new Point2D.Double(0, 0);

    // act
    Point2D end = GeometricUtilities.project(start, angle, 1);
    double actualX = end.getX();

    // assert
    assertEquals(expectedX, actualX, 0.001);
  }

  @ParameterizedTest(name="testProjectByAngle_yCoordinate angle={0}, expectedY={1}")
  @CsvSource({
          "90.0d, 0d",
          "180.0d, -1.0d",
          "270.0d, 0",
          "360, 1.0d"
  })
  public void testProjectionByAngle_yCoordinate(double angle, double expectedY){
    // arrange
    final Point2D start = new Point2D.Double(0, 0);

    // act
    Point2D end = GeometricUtilities.project(start, angle, 1);
    double actualY = end.getY();

    // assert
    assertEquals(expectedY, actualY, 0.001);
  }
  
  @ParameterizedTest(name="testProjectionByScalar_xCoordinate x={0}, y={1}, scalar={2}, expectedX={3}")
  @CsvSource({
          "10.0d, 10.0d, 1.414d, 1.0d",
          "10.0d, 10.0d, 14.142d, 10.0d",
          "-10.0d, -10.0d, 3.536d, -2.5d",
          "-10.0d, -10.0d, 14.142d, -10.0d"
  })
  public void testProjectionByScalar_xCoordinate(double x, double y, double scalar, double expectedX){
    // arrange
    final Point2D start = new Point2D.Double(0, 0);
    final Point2D target = new Point2D.Double(x, y);

    // act
    Point2D end = GeometricUtilities.project(start, target, scalar);
    double actualX = end.getX();

    // assert
    assertEquals(expectedX, actualX, 0.001);
  }

  @ParameterizedTest(name="testProjectionByScalar_yCoordinate x={0}, y={1}, scalar={2}, expectedY={3}")
  @CsvSource({
          "10.0d, 10.0d, 1.414d, 1.0d",
          "10.0d, 10.0d, 14.142d, 10.0d",
          "-10.0d, -10.0d, 3.536d, -2.5d",
          "-10.0d, -10.0d, 14.142d, -10.0d"
  })
  public void testProjectionByScalar_yCoordinate(double x, double y, double scalar, double expectedY){
    // arrange
    final Point2D start = new Point2D.Double(0, 0);
    final Point2D target = new Point2D.Double(x, y);

    // act
    Point2D end = GeometricUtilities.project(start, target, scalar);
    double actualY = end.getY();

    // assert
    assertEquals(expectedY, actualY, 0.001);
  }

  @ParameterizedTest(name="testDeltaX angle={0}, expectedDeltaX={1}")
  @CsvSource({
          "45.0d, 0.70656418800354d",
          "0, -7.670362E-4d",
          "-45, -0.70764893d",
          "360, -7.670362E-4d"
  })
  public void testDeltaX(double angle, double expectedDeltaX){
    // arrange, act
    double actualDeltaX = GeometricUtilities.getDeltaX(angle);

    // assert
    assertEquals(expectedDeltaX, actualDeltaX, 0.001d);
  }

  @ParameterizedTest(name = "testDeltaY angle={0}, expectedDeltaY={1}")
  @CsvSource({
          "0, 1.0d",
          "30, 0.8658d",
          "360, 1.0d",
          "-42, 0.7435d"
  })
  public void testDeltaY(double angle, double expectedDeltaY){
    // act
    double actualDeltaY = GeometricUtilities.getDeltaY(angle);

    // assert
    assertEquals(expectedDeltaY, actualDeltaY, 0.001d);
  }

  @ParameterizedTest(name = "testContains")
  @MethodSource("getContainsArguments")
  public void testContains(Rectangle2D rectangle, Point2D point, boolean expectedResult){
    // act
    boolean contains = GeometricUtilities.contains(rectangle, point);

    // assert
    assertEquals(expectedResult, contains);
  }

  @ParameterizedTest(name = "testGetConstrainingLines {0}, lineNumber={1}, expectedStart ({2},{3}), expectedEnd ({4},{5})")
  @CsvSource({
          "topLeft-topRight, 0, 0, 0, 0, 10",
          "topRight-bottomRight, 1, 0, 10, 10, 10",
          "bottomRight-bottomLeft, 2, 10, 10, 10, 0",
          "bottomLeft-topLeft, 3, 10, 0, 0, 0"
  })
  public void testGetConstrainingLines(String caption, int lineNumber, double expectedX1, double expectedY1, double expectedX2, double expectedY2){
    // arrange
    Rectangle2D bounds = new Rectangle2D.Double(0, 0, 10, 10);
    Area area = new Area(bounds);

    // act
    List<Line2D.Double> constrainingLines = GeometricUtilities.getConstrainingLines(area);
    Line2D.Double line = constrainingLines.get(lineNumber);

    // assert
    assertEquals(expectedX1, line.x1);
    assertEquals(expectedY1, line.y1);
    assertEquals(expectedX2, line.x2);
    assertEquals(expectedY2, line.y2);
  }

  @ParameterizedTest(name = "testGetPointsBetweenPoints start=({0},{1}) end=({2},{3})")
  @CsvSource({
          "0, 0, 10, 10",
          "0, 0, 0, 10",
          "0, 0, 10, 0",
          "2.0, 7, -1, -2",
          "-9, -8, 2, 19",
          "0, 0, 1000, 1"
  })
  public void testGetPointsBetweenPoints(double x1, double y1, double x2, double y2){
    // arrange
    Point2D start = new Point2D.Double(x1, y1);
    Point2D end = new Point2D.Double(x2, y2);
    Line2D line = new Line2D.Double(start.getX(), start.getY(), end.getX(), end.getY());

    // act
    List<Point2D> pointsBetweenPoints = GeometricUtilities.getPointsBetweenPoints(start, end);

    // assert, checks if all generated points are within margin of error of the Bresenham algorithm (0.5)
    assertTrue(pointsBetweenPoints.stream().allMatch(point -> line.ptLineDist(point) < 0.5d));
  }

  @ParameterizedTest(name = "testGetPerpendicularIntersection lineStart=({0},{1}), lineEnd=({2},{3}), point=({4},{5})")
  @CsvSource({
          "1.0d, 1.0d, 9.0d, 1.0d, 4.0d, 3.0d", // orthogonal
          "0.0d, 0.0d, 12.75d, -13.5d, 5.32d, 4.21d", // crooked
          "1.0d, 1.0d, 3.0d, 3.0d, 2.0d, 2.0d" // point on the line
  })
  public void testGetPerpendicularIntersection(double x1, double y1, double x2, double y2, double pointX, double pointY){
    // arrange
    Point2D point = new Point2D.Double(pointX, pointY);
    Line2D line = new Line2D.Double(x1, y1, x2, y2);
    double distanceToLine = line.ptLineDist(point); // shortest distance between the point and the line

    // act
    Point2D intersection = GeometricUtilities.getPerpendicularIntersection(point, line);

    // assert, if the distance from the line to the point is equal to the distance between the intersection and the
    //  point, this means that the intersection point is correct (distance measured at closest point, which is true
    //  if the intersection line is perpendicular)
    assertEquals(distanceToLine, intersection.distance(point), 0.000001d);
  }

  @ParameterizedTest(name = "testGetPointOnCircle circleCenter=({0},{1}), circleRadius={2}, angle={3}, expectedPoint=({4},{5})")
  @CsvSource({
          "0d, 0d, 1.0d, 0d, 1.0d, 0d", // 0 degrees
          "0d, 0d, 1.0d, 45d, 0.7071d, 0.7071d", // 45 degrees
          "0d, 0d, 1.0d, 90d, 0d, 1.0d", // 90 degrees
          "0d, 0d, 1.0d, 180d, -1.0d, 0d", //  180 degrees
          "0d, 0d, 1.0d, 270d, 0d, -1.0d", // 270 degrees
          "0d, 0d, 1.0d, 360d, 1.0d, 0d", // 360 degrees = 0 degrees
          "1.5d, 2.74d, 2.0d, 84.84d, 1.679874d, 4.73189d" // 84.84 degrees, offset center
  })
  public void testGetPointOnCircle(double centerX, double centerY, double radius, double angle, double expectedX, double expectedY){
    // arrange
    Point2D center = new Point2D.Double(centerX, centerY);

    // act
    Point2D pointOnCircle = GeometricUtilities.getPointOnCircle(center, radius, angle);

    // assert
    assertEquals(expectedX, pointOnCircle.getX(), 0.0001d);
    assertEquals(expectedY, pointOnCircle.getY(), 0.0001d);
  }

  @Test
  public void testGetAveragePosition(){
    // arrange
    Collection<Point2D> points = new ArrayList<>();
    points.add(new Point2D.Double(2.03d, 4.93d));
    points.add(new Point2D.Double(-1.78d, 3.12d));
    points.add(new Point2D.Double(1.053d, 0d));

    // act
    Point2D avgPosition = GeometricUtilities.getAveragePosition(points);

    // assert
    assertEquals(0.4343d, avgPosition.getX(), 0.0001d);
    assertEquals(2.6833d, avgPosition.getY(), 0.0001d);
  }

  @Test
  public void testGetAveragePositionEmpty(){
    // arrange
    Collection<Point2D> points = new ArrayList<>();

    // act
    Point2D avgPosition = GeometricUtilities.getAveragePosition(points);

    // assert
    assertNull(avgPosition);
  }

  /**
   * TODO: Sometimes, floating point precision can stand in the way of calculating accurate intersections, this
   *  should probably be investigated. @see{@link java.awt.geom.RectangularShape#intersects(Rectangle2D)}
   *  The inaccurate cases are marked in @see{getIntersectsEllipseArguments}
   */
  @ParameterizedTest(name = "testIntersectsEllipse {0}")
  @MethodSource("getIntersectsEllipseArguments")
  public void testIntersectsEllipse(String caption, Ellipse2D baseEllipse, Ellipse2D intersectionEllipse, boolean expectedIntersection){
    // act
    boolean intersects = GeometricUtilities.intersects(baseEllipse, intersectionEllipse);

    // assert
    assertEquals(expectedIntersection, intersects);
  }

  @Test
  public void testGetPoints(){
    // arrange
    Path2D path = new Path2D.Double();
    List<Point2D> referencePoints = new ArrayList<>();

    referencePoints.add(new Point2D.Double(7.83d, 9.81d));
    referencePoints.add(new Point2D.Double(-12.16d, -9.32d));
    referencePoints.add(new Point2D.Double(101.53d, -22.347d));

    path.moveTo(referencePoints.get(0).getX(), referencePoints.get(0).getY());

    referencePoints.forEach(referencePoint -> path.lineTo(referencePoint.getX(), referencePoint.getY()));

    // act
    List<Point2D> points = GeometricUtilities.getPoints(path);

    // assert
    assertTrue(points.containsAll(referencePoints));
  }

  @ParameterizedTest(name = "testDistance point1=({0},{1}), point2=({2},{3})")
  @MethodSource("getDistanceArguments")
  public void testDistanceCoordinates(double x1, double y1, double x2, double y2, double expectedDistance){
    // act
    double actualDistance = GeometricUtilities.distance(x1, y1, x2, y2);

    // assert
    assertEquals(expectedDistance, actualDistance, 0.00001d);
  }

  @ParameterizedTest(name = "testDistance point1=({0},{1}), point2=({2},{3})")
  @MethodSource("getDistanceArguments")
  public void testDistancePoints(double x1, double y1, double x2, double y2, double expectedDistance){
    // arrange
    Point2D point1 = new Point2D.Double(x1, y1);
    Point2D point2 = new Point2D.Double(x2, y2);

    // act
    double actualDistance = GeometricUtilities.distance(point1,point2);

    // assert
    assertEquals(expectedDistance, actualDistance, 0.00001d);
  }

  @ParameterizedTest(name = "testDistanceRectanglePoint point=({0},{1})")
  @CsvSource({
          "-9.53d, 15.23d, 29.9870105d",
          "23.95d, -8.23d, 19.5528105d",
          "20.15d, 10.95d, 0d"
  })
  public void testDistanceRectanglePoint(double px, double py, double expectedDistance){
    // arrange
    Rectangle2D rectangle = new Rectangle2D.Double(20.15d, 10.95d, 10, 20);
    Point2D point = new Point2D.Double(px, py);

    // act
    double actualDistance = GeometricUtilities.distance(rectangle, point);

    // assert
    assertEquals(expectedDistance, actualDistance, 0.000001d);
  }

  /**
   * This method is used to provide input arguments for @see{testContains}
   * @return Arguments for the unit test
   */
  @SuppressWarnings("unused")
  private static Stream<Arguments> getContainsArguments(){
    // arrange
    return Stream.of(
            Arguments.of(new Rectangle2D.Double(0, 0, 10, 10), new Point2D.Double(9.9d, 9.9d), true), // both in
            Arguments.of(new Rectangle2D.Double(0, 0, 10, 10), new Point2D.Double(0, 0), true), // boundary top left
            Arguments.of(new Rectangle2D.Double(0, 0, 10, 10), new Point2D.Double(10, 10), true), // boundary bottom right
            Arguments.of(new Rectangle2D.Double(0, 0, 10, 10), new Point2D.Double(10, 0), true), // boundary top right
            Arguments.of(new Rectangle2D.Double(0, 0, 10, 10), new Point2D.Double(0, 10), true), // boundary bottom left
            Arguments.of(new Rectangle2D.Double(0, 0, 10, 10), new Point2D.Double(-0.1d, -0.1d), false), // both out
            Arguments.of(new Rectangle2D.Double(0, 0, 10, 10), new Point2D.Double(10.1d, 10.1d), false), // both out
            Arguments.of(new Rectangle2D.Double(0, 0, 10, 10), new Point2D.Double(9.9d, 10.1d), false), // x in y out
            Arguments.of(new Rectangle2D.Double(0, 0, 10, 10), new Point2D.Double(10.1d, 9.9d), false) // y in x out
    );
  }

  /**
   * Supplies the test arguments for @see{testIntersectsEllipse}
   * @return Arguments for the unit test
   */
  @SuppressWarnings("unused")
  private static Stream<Arguments> getIntersectsEllipseArguments(){
    // arrange
    return Stream.of(
            Arguments.of("both ellipses, no contact", new Ellipse2D.Double(0, 0, 10.75d, 20.0d), new Ellipse2D.Double(10.76d, 0, 10.75d, 20.0d), false),
            Arguments.of("both ellipses, touch (at one point)", new Ellipse2D.Double(0, 0, 10.75d, 20.0d), new Ellipse2D.Double(10.75d, 0, 10.75d, 20.0d), false),  // calculation inaccurate
            Arguments.of("both ellipses, intersect", new Ellipse2D.Double(0, 0, 10.75d, 20.0d), new Ellipse2D.Double(10.74d, 0, 10.75d, 20.0d), true),
            Arguments.of("both circles, no contact", new Ellipse2D.Double(0, 0, 10.75d, 10.0d), new Ellipse2D.Double(10.76d, 0, 10.75d, 10.75d), false),
            Arguments.of("both circles, touch (at one point)", new Ellipse2D.Double(0, 0, 10.75d, 10.75d), new Ellipse2D.Double(10.75d, 0, 10.75d, 10.75d), true),
            Arguments.of("both circles, intersect", new Ellipse2D.Double(0, 0, 10.75d, 10.75d), new Ellipse2D.Double(10.74d, 0, 10.75d, 10.75d), true),
            Arguments.of("both circles, far apart", new Ellipse2D.Double(-1.56d, -9.265d, 10.75d, 10.75d), new Ellipse2D.Double(999.0d, 1024.75d, 10.75d, 10.75d), false),
            Arguments.of("circle, ellipse, no contact", new Ellipse2D.Double(0, 0, 10.75d, 10.75d), new Ellipse2D.Double(10.76d, 0, 10.75d, 20.0d), false),
            Arguments.of("circle, ellipse, touch (at one point)", new Ellipse2D.Double(0, 0, 10.75d, 10.75d), new Ellipse2D.Double(10.75d, 4.625d, 10.75d, 20.0d), false), // calculation inaccurate
            Arguments.of("circle, ellipse, intersect", new Ellipse2D.Double(0, 0, 10.75d, 10.75d), new Ellipse2D.Double(9.9d, 0, 10.75d, 20.0d), true)
    );
  }

  /**
   * Provides the test arguments for @see testDistanceCoordinates and @see testDistancePoints
   * @return Arguments for the unit test
   */
  @SuppressWarnings("unused")
  private static Stream<Arguments> getDistanceArguments(){
    return Stream.of(
            Arguments.of(0, 0, 0, 0, 0),
            Arguments.of(2.756d, 12.5635d, -1.246d, 20.822d, 9.177082d)
    );
  }
}
