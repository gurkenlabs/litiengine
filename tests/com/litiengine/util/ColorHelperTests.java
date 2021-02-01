package com.litiengine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

public class ColorHelperTests {

  @Test
  public void testMalformedColorHexString() {
    Logger.getLogger(ColorHelper.class.getName()).setUseParentHandlers(false);

    String red = "~#ff0000";
    String red2 = "#ff0000000";
    Color redDecoded = ColorHelper.decode(red);
    Color redDecoded2 = ColorHelper.decode(red2);
    assertNull(redDecoded);
    assertNull(redDecoded2);
  }

  @Test
  public void testColorFromHexString() {
    String red = "#ff0000";
    String green = "#00ff00";
    String blue = "#0000ff";

    Color redDecoded = ColorHelper.decode(red);
    Color greenDecoded = ColorHelper.decode(green);
    Color blueDecoded = ColorHelper.decode(blue);

    assertEquals(Color.RED, redDecoded);
    assertEquals(Color.GREEN, greenDecoded);
    assertEquals(Color.BLUE, blueDecoded);
  }

  @Test
  public void testColorFromAlphaHexString() {
    String red200 = "#c8ff0000";

    Color redDecoded = ColorHelper.decode(red200);

    Color alphaRed = new Color(255, 0, 0, 200);
    assertEquals(alphaRed.getRed(), redDecoded.getRed());
    assertEquals(alphaRed.getGreen(), redDecoded.getGreen());
    assertEquals(alphaRed.getBlue(), redDecoded.getBlue());
    assertEquals(alphaRed.getAlpha(), redDecoded.getAlpha());
  }

  @Test
  public void testSolidColorFromAlphaHexString() {
    String red200 = "#c8ff0000";
    String green00 = "#c800ff00";
    String blue200 = "#c80000ff";

    Color redDecoded = ColorHelper.decode(red200, true);
    Color greenDecoded = ColorHelper.decode(green00, true);
    Color blueDecoded = ColorHelper.decode(blue200, true);

    Color solidAlphaRed = new Color(228, 0, 0);
    Color solidAlphaGreen = new Color(0, 228, 0);
    Color solidAlphaBlue = new Color(0, 0, 228);

    assertEquals(solidAlphaRed, redDecoded);
    assertEquals(solidAlphaGreen, greenDecoded);
    assertEquals(solidAlphaBlue, blueDecoded);
  }

  @Test
  public void testHexStringWithoutHashtag() {
    String red = "ff0000";
    String green = "00ff00";
    String blue = "0000ff";

    Color redDecoded = ColorHelper.decode(red);
    Color greenDecoded = ColorHelper.decode(green);
    Color blueDecoded = ColorHelper.decode(blue);

    assertEquals(Color.RED, redDecoded);
    assertEquals(Color.GREEN, greenDecoded);
    assertEquals(Color.BLUE, blueDecoded);
  }

  @Test
  public void testEncodeColor() {
    String redEncoded = ColorHelper.encode(Color.RED);
    String greenEncoded = ColorHelper.encode(Color.GREEN);
    String blueEncoded = ColorHelper.encode(Color.BLUE);
    String invisibleBlack = ColorHelper.encode(new Color(0, 0, 0, 0));

    assertEquals("#ff0000", redEncoded);
    assertEquals("#00ff00", greenEncoded);
    assertEquals("#0000ff", blueEncoded);
    assertEquals("#00000000", invisibleBlack);
    assertNull(ColorHelper.encode(null));
  }

  @Test
  public void testEncodeAlphaColor() {
    String redEncoded = ColorHelper.encode(new Color(255, 0, 0, 200));
    String greenEncoded = ColorHelper.encode(new Color(0, 255, 0, 200));
    String blueEncoded = ColorHelper.encode(new Color(0, 0, 255, 200));

    assertEquals("#c8ff0000", redEncoded);
    assertEquals("#c800ff00", greenEncoded);
    assertEquals("#c80000ff", blueEncoded);
  }

  @Test
  public void testRgbBounds() {
    int negative = -10;
    int zero = 0;
    int inRange = 158;
    int max = 255;
    int outOfRange = 300;

    int rgbNegative = ColorHelper.ensureColorValueRange(negative);
    int rgbZero = ColorHelper.ensureColorValueRange(zero);
    int rgbInRange = ColorHelper.ensureColorValueRange(inRange);
    int rgbMax = ColorHelper.ensureColorValueRange(max);
    int rgbOutOfRange = ColorHelper.ensureColorValueRange(outOfRange);

    assertEquals(0, rgbNegative);
    assertEquals(0, rgbZero);
    assertEquals(158, rgbInRange);
    assertEquals(rgbMax, 255);
    assertEquals(rgbOutOfRange, 255);
  }
}
