package de.gurkenlabs.litiengine.util.geom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Dimension2D;
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

    assertEquals(new Point2D.Double(0, 0.5), mid);
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
}
