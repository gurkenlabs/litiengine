package de.gurkenlabs.litiengine.util.geom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Arc2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

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
  public void testGetCenterPointPoint(){
    // arrange
    Point2D point1 = new Point2D.Double(0, 0);
    Point2D point2 = new Point2D.Double(0, 1);

    // act
    Point2D mid = GeometricUtilities.getCenter(point1, point2);

    // assert
    assertEquals(new Point2D.Double(0, 0.5), mid);
  }

  @Test
  public void testGetCenterLine(){
    // arrange
    Line2D line = new Line2D.Double(new Point2D.Double(0,0), new Point2D.Double(0,1));

    // act
    Point2D mid = GeometricUtilities.getCenter(line);

    // assert
    assertEquals(new Point2D.Double(0, 0.5), mid);
  }

  @Test
  public void testGetCenterDouble(){
    // arrange
    double x1 = 0;
    double y1 = 0;
    double x2 = 1;
    double y2 = 1;

    // act
    Point2D mid = GeometricUtilities.getCenter(x1, y1, x2, y2);

    // assert
    assertEquals(new Point2D.Double(0.5, 0.5), mid);
  }

  @ParameterizedTest
  @MethodSource("getCenterRectangularShapeArguments")
  public void testGetCenterRectangularShape(String name, RectangularShape shape, double expectedX, double expectedY){
    // act
    Point2D center = GeometricUtilities.getCenter(shape);

    // assert
    assertEquals(new Point2D.Double(expectedX, expectedY), center);
  }

  /**
   * This method is used to provide arguments for {@link #testGetCenterRectangularShape(String, RectangularShape, double, double)}
   * @return Test arguments
   */
  @SuppressWarnings("unused")
  private static Stream<Arguments> getCenterRectangularShapeArguments(){
    return Stream.of(
            Arguments.of("Arc", new Arc2D.Double(0, 0, 1, 1, 1, 1, Arc2D.OPEN), 0.5, 0.5),
            Arguments.of("Ellipse", new Ellipse2D.Double(0, 0, 1, 1), 0.5, 0.5),
            Arguments.of("Circle", GeometricUtilities.getCircle(new Point2D.Double(0.5d, 0.5d), 0.5d), 0.5, 0.5)
    );
  }

  @ParameterizedTest(name = "testGetCenterRectangle x1={0}, y1={1}, x2={2}, y2={3}")
  @CsvSource({
          "0, 0, 1, 1, 0.5, 0.5",
          "5, 5, 10, 10, 10, 10",
          "-5, -5, 10, 10, 0, 0"
  })
  public void testGetCenterRectangle(double x1, double y1, double x2, double y2, double expectedX, double expectedY){
    // arrange
    Rectangle2D rectangle = new Rectangle2D.Double(x1, y1, x2, y2);

    // act
    Point2D mid = GeometricUtilities.getCenter(rectangle);

    // assert
    assertEquals(new Point2D.Double(expectedX, expectedY), mid);
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
    assertEquals(expectedDeltaX, (float) actualDeltaX, 0.001);
  }
}
