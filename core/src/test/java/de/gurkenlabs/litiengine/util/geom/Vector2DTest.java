package de.gurkenlabs.litiengine.util.geom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Point2D;
import org.junit.jupiter.api.Test;

class Vector2DTest {

  /** Test add-function in Vector2D class */
  @Test
  void testVector2D() {
    double xValue = 10.0;
    double yValue = 5.0;
    Point2D point1 = new Point2D.Double(0, 0);
    ;
    Point2D point2 = new Point2D.Double(xValue, yValue);
    ;

    Vector2D vectorStandard = new Vector2D();
    Vector2D vectorDouble = new Vector2D(xValue, yValue);
    Vector2D vectorPoint2D = new Vector2D(point1, point2);

    Vector2D vectorResult1 = vectorStandard.add(vectorDouble);
    Vector2D vectorResult2 = vectorStandard.add(vectorPoint2D);

    assertEquals(vectorResult1.dX, xValue);
    assertEquals(vectorResult1.dY, yValue);
    assertEquals(vectorResult2.dX, xValue);
    assertEquals(vectorResult2.dY, yValue);
    assertEquals(vectorResult1.dX, vectorResult1.getX());
    assertEquals(vectorResult1.dY, vectorResult1.getY());
  }

  /** Test unitVector-function in Vector2D class */
  @Test
  void testVector2DUnit() {
    double xValue = 0.0;
    double yValue = 5.0;

    Vector2D vectorDouble = new Vector2D(xValue, yValue);
    Vector2D unitVector = vectorDouble.unitVector();
    assertEquals(unitVector.getY(), 1.0);
    assertEquals(unitVector.getX(), 0.0);

    yValue = 0.0;
    Vector2D zeroLengthVector = new Vector2D(xValue, yValue);
    unitVector = zeroLengthVector.unitVector();
    assertEquals(unitVector.getX(), 0.0);
    assertEquals(unitVector.getY(), 0.0);
  }

  /** Test toString method */
  @Test
  void testVector2DToString() {
    double xValue = 10.0;
    double yValue = 5.0;
    String expected = "Vector2D(10.0, 5.0)";

    Vector2D vectorDouble = new Vector2D(xValue, yValue);

    assertEquals(expected, vectorDouble.toString());
  }

  /** Test normal vector */
  @Test
  void testVector2DNormal() {
    double xValue = 5.0;
    double yValue = 0.0;
    double expectedX = 0.0;
    double expectedY = -5.0;

    Vector2D vector = new Vector2D(xValue, yValue);
    Vector2D result = vector.normalVector();

    assertEquals(result.getX(), expectedX);
    assertEquals(result.getY(), expectedY);
  }

  /** Test scale vector */
  @Test
  void testVector2DScale() {
    double xValue = 5.0;
    double yValue = 0.0;
    double scalefactor = 3.0;
    double expectedX = 15.0;
    double expectedY = 0.0;
    Vector2D vector = new Vector2D(xValue, yValue);
    Vector2D result = vector.scale(scalefactor);

    assertEquals(result.getX(), expectedX);
    assertEquals(result.getY(), expectedY);
  }

  /** Test dot product */
  @Test
  void testVector2DDot() {
    double xValue1 = 5.0;
    double yValue1 = 10.0;
    double xValue2 = 3.0;
    double yValue2 = 2.0;
    double expected = 35.0;

    Vector2D vector1 = new Vector2D(xValue1, yValue1);
    Vector2D vector2 = new Vector2D(xValue2, yValue2);
    double result = vector1.dotProduct(vector2);
    assertEquals(result, expected);
  }

  /** Test sub */
  @Test
  void testVector2DSub() {
    double xValue1 = 5.0;
    double yValue1 = 10.0;
    double xValue2 = 3.0;
    double yValue2 = 2.0;
    double expectedX = 2.0;
    double expectedY = 8.0;

    Vector2D vector1 = new Vector2D(xValue1, yValue1);
    Vector2D vector2 = new Vector2D(xValue2, yValue2);
    Vector2D result = vector1.sub(vector2);

    assertEquals(result.getX(), expectedX);
    assertEquals(result.getY(), expectedY);
  }
}
