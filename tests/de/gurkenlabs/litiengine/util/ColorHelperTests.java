package de.gurkenlabs.litiengine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
  public void testRedFromAlphaHexString() {
    String red200 = "#c8ff0000";

    Color redDecoded = ColorHelper.decode(red200);

    Color alphaRed = new Color(255, 0, 0, 200);
    assertEquals(alphaRed.getRed(), redDecoded.getRed());
  }

  @Test
  public void testGreenFromAlphaHexString() {
    String red200 = "#c8ff0000";

    Color redDecoded = ColorHelper.decode(red200);

    Color alphaRed = new Color(255, 0, 0, 200);
    assertEquals(alphaRed.getGreen(), redDecoded.getGreen());
  }

  @Test
  public void testBlueFromAlphaHexString() {
    String red200 = "#c8ff0000";

    Color redDecoded = ColorHelper.decode(red200);

    Color alphaRed = new Color(255, 0, 0, 200);
    assertEquals(alphaRed.getBlue(), redDecoded.getBlue());
  }

  @Test
  public void testAlphaFromAlphaHexString() {
    String red200 = "#c8ff0000";

    Color redDecoded = ColorHelper.decode(red200);

    Color alphaRed = new Color(255, 0, 0, 200);
    assertEquals(alphaRed.getAlpha(), redDecoded.getAlpha());
  }

  @Test
  public void testSolidColorFromAlphaHexString() {
    String red200 = "#c8ff0000";
    String green00 = "#c800ff00";
    String blue200 = "#c80000ff";
    String emptyColor = "";
    String nullColor = null;

    Color redDecoded = ColorHelper.decode(red200, true);
    Color greenDecoded = ColorHelper.decode(green00, true);
    Color blueDecoded = ColorHelper.decode(blue200, true);
    Color emptyDecoded = ColorHelper.decode(emptyColor, true);
    Color nullDecoded = ColorHelper.decode(nullColor, true);

    Color solidAlphaRed = new Color(228, 0, 0);
    Color solidAlphaGreen = new Color(0, 228, 0);
    Color solidAlphaBlue = new Color(0, 0, 228);

    assertEquals(solidAlphaRed, redDecoded);
    assertEquals(solidAlphaGreen, greenDecoded);
    assertEquals(solidAlphaBlue, blueDecoded);
    assertNull(emptyDecoded);
    assertNull(nullDecoded);
  }

  @Test
  public void testHexStringWithoutHashtag() {
    String red = "ff0000";
    String green = "00ff00";
    String blue = "0000ff";
    String nullColor = "000";

    Color redDecoded = ColorHelper.decode(red);
    Color greenDecoded = ColorHelper.decode(green);
    Color blueDecoded = ColorHelper.decode(blue);
    Color nullDecoded = ColorHelper.decode(nullColor);

    assertEquals(Color.RED, redDecoded);
    assertEquals(Color.GREEN, greenDecoded);
    assertEquals(Color.BLUE, blueDecoded);
    assertNull(nullDecoded);
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

  @ParameterizedTest(name="testRgbBounds {0}, colorValue={1}, expectedRgb={2}")
  @CsvSource({
          "Negative, -10, 0",
          "Zero, 0, 0",
          "InRange, 158, 158",
          "Max, 255, 255",
          "OutOfRange, 300, 255"
  })
  public void testRgbBounds(String rgbBound, int colorValue, int expectedRgb){
    // arrange, act
    int actualRgb = ColorHelper.ensureColorValueRange(colorValue);

    // assert
    assertEquals(expectedRgb, actualRgb);
  }

  @Test
  public void testPremultiply(){
    Color color = new Color(225, 0, 0);
    assertEquals(new Color(225, 0, 0), ColorHelper.premultiply(color));
  }

}
