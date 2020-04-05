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

  @Test
  public void testCalcRotationAngles() {
    double rotationAngle = GeometricUtilities.calcRotationAngleInDegrees(new Point2D.Double(0, 0), new Point2D.Double(1, 1));
    double rotationAngle2 = GeometricUtilities.calcRotationAngleInDegrees(new Point2D.Double(0, 0), new Point2D.Double(1, 0));
    double rotationAngle3 = GeometricUtilities.calcRotationAngleInDegrees(new Point2D.Double(0, 0), new Point2D.Double(0, 1));
    double rotationAngle4 = GeometricUtilities.calcRotationAngleInDegrees(new Point2D.Double(0, 0), new Point2D.Double(-1, -1));
    double rotationAngle5 = GeometricUtilities.calcRotationAngleInDegrees(new Point2D.Double(0, 0), new Point2D.Double(-1, 0));
    double rotationAngle6 = GeometricUtilities.calcRotationAngleInDegrees(new Point2D.Double(0, 0), new Point2D.Double(0, -1));
    double rotationAngle7 = GeometricUtilities.calcRotationAngleInDegrees(new Point2D.Double(0, 0), new Point2D.Double(1, -1));
    double rotationAngle8 = GeometricUtilities.calcRotationAngleInDegrees(new Point2D.Double(0, 0), new Point2D.Double(-1, 1));

    assertEquals(45, (float) rotationAngle);
    assertEquals(90, (float) rotationAngle2);
    assertEquals(0, (float) rotationAngle3);
    assertEquals(225, (float) rotationAngle4);
    assertEquals(270, (float) rotationAngle5);
    assertEquals(180, (float) rotationAngle6);
    assertEquals(135, (float) rotationAngle7);
    assertEquals(315, (float) rotationAngle8);
  }

  @Test
  public void testGetMidPoint() {
    Point2D mid = GeometricUtilities.getMidPoint(new Point2D.Double(0, 0), new Point2D.Double(0, 1));
    Point2D mid2 = GeometricUtilities.getMidPoint(new Line2D.Double(new Point2D.Double(0,0), new Point2D.Double(0,1)));
    Point2D mid3 = GeometricUtilities.getMidPoint(0, 0, 1, 1);
    Point2D mid4 = GeometricUtilities.getMidPoint(GeometricUtilities.getCircle(new Point2D.Double(0.5d, 0.5d), 0.5d));
    Point2D mid5 = GeometricUtilities.getMidPoint(new Ellipse2D.Double(0, 0, 1, 1));
    Point2D mid6 = GeometricUtilities.getMidPoint(new Rectangle2D.Double(0, 0, 1, 1));
    Point2D mid7 = GeometricUtilities.getMidPoint(new Arc2D.Double(0, 0, 1, 1, 1, 1, Arc2D.OPEN));
    
    Rectangle2D rectangle8 = new Rectangle2D.Double(5, 5, 10, 10);
    Point2D mid8 = GeometricUtilities.getMidPoint(rectangle8);
    
    Rectangle2D rectangle9 = new Rectangle2D.Double(-5, -5, 10, 10);
    Point2D mid9 = GeometricUtilities.getMidPoint(rectangle9);
    
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
  
  @Test
  public void testProjectionByAngle() {
    final Point2D start = new Point2D.Double(0, 0);
    
    Point2D end = GeometricUtilities.project(start, 90, 1);
    Point2D end2 = GeometricUtilities.project(start, 180, 1);
    Point2D end3 = GeometricUtilities.project(start, 270, 1);
    Point2D end4 = GeometricUtilities.project(start, 360, 1);

    assertEquals(1, end.getX(), 0.001);
    assertEquals(0, end.getY(), 0.001);
    
    assertEquals(0, end2.getX(), 0.001);
    assertEquals(-1, end2.getY(), 0.001);
    
    assertEquals(-1, end3.getX(), 0.001);
    assertEquals(0, end3.getY(), 0.001);
    
    assertEquals(0, end4.getX(), 0.001);
    assertEquals(1, end4.getY(), 0.001);
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
}
