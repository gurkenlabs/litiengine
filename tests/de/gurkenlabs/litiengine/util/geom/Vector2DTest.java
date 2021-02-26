package de.gurkenlabs.litiengine.util.geom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Point2D;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class Vector2DTest {

    /**
     * Test add-function in Vector2D class
     */
    @Test
    public void testVector2D() {
        double xValue = 10.0;
        double yValue = 5.0;
        Point2D point1 = new Point2D.Double(0, 0);;
        Point2D point2 = new Point2D.Double(xValue, yValue);;

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

    /**
     * Test unitVector-function in Vector2D class
     */
    @Test
    public void testVector2DUnit(){
        double xValue = 0.0;
        double yValue = 5.0;

        Vector2D vectorDouble = new Vector2D(xValue, yValue);
        Vector2D unitVector = vectorDouble.unitVector();
        assertEquals(unitVector.getY(), 1.0);
        assertEquals(unitVector.getX(), 0.0);

        yValue = 0.0;
        Vector2D zeroLengthVector = new Vector2D(xValue,yValue);
        unitVector = zeroLengthVector.unitVector();
        assertEquals(unitVector.getX(), 0.0);
        assertEquals(unitVector.getY(), 0.0);

    }
}
