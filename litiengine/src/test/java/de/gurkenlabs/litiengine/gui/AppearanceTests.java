package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AppearanceTests {

  @Test
  void testEquals() {
    Appearance appearance = new Appearance();
    Color color = Color.BLUE;
    assertNotEquals(appearance, color);
  }

  @Test
  void testGetBackgroundPaintBackground() {
    Appearance appearance = new Appearance(Color.BLUE, Color.RED);
    assertEquals(Color.RED, appearance.getBackgroundPaint(0, 0));
  }

  @Test
  void testGetBackgroundPaintBackgroundNull() {
    Appearance appearance = new Appearance(Color.BLUE, null);
    appearance.setBackgroundColor2(Color.GREEN);

    assertEquals(Color.GREEN, appearance.getBackgroundPaint(0, 0));
  }

  @Test
  void testBackgroundPaintTransparent() {
    Appearance appearance = new Appearance(null);
    assertNull(appearance.getBackgroundPaint(0, 0));
  }

  @Test
  void testGradientPaintIsCachedAndInvalidated() {
    Appearance appearance = new Appearance(Color.RED, Color.BLUE);
    appearance.setBackgroundColor2(Color.YELLOW);

    Paint cachedPaint = appearance.getBackgroundPaint(100, 100);
    assertSame(cachedPaint, appearance.getBackgroundPaint(100, 100));

    appearance.setBackgroundColor1(Color.GREEN);
    Paint colorChangedPaint = appearance.getBackgroundPaint(100, 100);
    assertNotSame(cachedPaint, colorChangedPaint);

    appearance.setHorizontalBackgroundGradient(true);
    assertNotSame(colorChangedPaint, appearance.getBackgroundPaint(100, 100));
    assertNotSame(appearance.getBackgroundPaint(100, 100), appearance.getBackgroundPaint(200, 100));
  }

  @ParameterizedTest(name = "testGetBackgroundPaint gradient is {0}")
  @CsvSource({"true, 50, 0", "false, 0, 50"})
  void testGetBackgroundPaintGradientTrue(
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
