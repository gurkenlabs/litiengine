package de.gurkenlabs.litiengine.util.geom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Arc2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
  public void testProjectByAngle_xCoordinate(double angle, double expectedX){
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
  public void testProjectByAngle_yCoordinate(double angle, double expectedY){
    // arrange
    final Point2D start = new Point2D.Double(0, 0);

    // act
    Point2D end = GeometricUtilities.project(start, angle, 1);
    double actualY = end.getY();

    // assert
    assertEquals(expectedY, actualY, 0.001);
  }
  
  @Test
  public void testProjectionByScalar() {
    final Point2D start = new Point2D.Double(0, 0);
    final Point2D target = new Point2D.Double(10, 10);
    final Point2D target2 = new Point2D.Double(-10, -10);
    
    Point2D end = GeometricUtilities.project(start, target, 1.414);
    Point2D end2 = GeometricUtilities.project(start, target2, 3.536);
    Point2D end3 = GeometricUtilities.project(start, target, 14.142);
    Point2D end4 = GeometricUtilities.project(start, target2, 14.142);
    
    assertEquals(1, end.getX(), 0.001);
    assertEquals(1, end.getY(), 0.001);
    
    assertEquals(-2.5, end2.getX(), 0.001);
    assertEquals(-2.5, end2.getY(), 0.001);
    
    assertEquals(10, end3.getX(), 0.001);
    assertEquals(10, end3.getY(), 0.001);
    
    assertEquals(-10, end4.getX(), 0.001);
    assertEquals(-10, end4.getY(), 0.001);
  }

  @Test
  public void testDeltaX() {
    double actualAngle1 = GeometricUtilities.getDeltaX(45);
    double actualAngle2 = GeometricUtilities.getDeltaX(0);
    double actualAngle3 = GeometricUtilities.getDeltaX(-45);
    double actualAngle4 = GeometricUtilities.getDeltaX(360);


    assertEquals(0.70656418800354, (float) actualAngle1, 0.0001);
    assertEquals(-7.670362E-4, (float) actualAngle2, 0.0001);
    assertEquals(-0.70764893, (float) actualAngle3, 0.0001);
    assertEquals(actualAngle2, actualAngle4);
  }
}
