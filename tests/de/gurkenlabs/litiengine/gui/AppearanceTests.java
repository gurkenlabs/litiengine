package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class AppearanceTests {

  @Test
  public void testEquals() {
    Appearance appearance = new Appearance();
    Color color = Color.BLUE;
    assertFalse(appearance.equals(color));
  }

  @Test
  public void testGetBackgroundPaintBackground() {
    Appearance appearance = new Appearance(Color.BLUE, Color.RED);
    assertEquals(Color.RED, appearance.getBackgroundPaint(0, 0));
  }

  @Test
  public void testGetBackgroundPaintBackgroundNull() {
    Appearance appearance = new Appearance(Color.BLUE, null);
    appearance.setBackgroundColor2(Color.GREEN);

    assertEquals(Color.GREEN, appearance.getBackgroundPaint(0, 0));
  }

  @Test
  public void testBackgroundPaintTransparent() {
    Appearance appearance = new Appearance(null);
    assertNull(appearance.getBackgroundPaint(0, 0));
  }

  @ParameterizedTest(name = "testGetBackgroundPaint gradient is {0}")
  @CsvSource({"true, 50, 0", "false, 0, 50"})
  public void testGetBackgroundPaintGradientTrue(
      boolean backgroundGradient, int expectedX, int expectedY) {
    // arrange
    Appearance appearance = new Appearance(Color.RED, Color.BLUE);
    appearance.setBackgroundColor2(Color.RED);
    appearance.setHorizontalBackgroundGradient(backgroundGradient);

    // act
    Paint paint = appearance.getBackgroundPaint(100, 100);

    // assert
    Point2D paintPoint = ((GradientPaint) paint).getPoint2();
    assertEquals(expectedX, paintPoint.getX());
    assertEquals(expectedY, paintPoint.getY());
  }
}
