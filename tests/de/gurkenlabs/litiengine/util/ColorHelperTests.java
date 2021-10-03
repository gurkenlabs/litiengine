package de.gurkenlabs.litiengine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ColorHelperTests {

  @Test
  void testMalformedColorHexString() {
    Logger.getLogger(ColorHelper.class.getName()).setUseParentHandlers(false);

    String red = "~#ff0000";
    String red2 = "#ff0000000";
    Color redDecoded = ColorHelper.decode(red);
    Color redDecoded2 = ColorHelper.decode(red2);
    assertNull(redDecoded);
    assertNull(redDecoded2);
  }

  @ParameterizedTest
  @MethodSource("getColorFromHexString")
  void testColorFromHexString(String colorHex, Color expectedColor) {
    Color colorDecoded = ColorHelper.decode(colorHex);
    assertEquals(expectedColor, colorDecoded);
  }

  private static Stream<Arguments> getColorFromHexString() {
    return Stream.of(
        Arguments.of("#ff0000", Color.RED),
        Arguments.of("#00ff00", Color.GREEN),
        Arguments.of("#0000ff", Color.BLUE));
  }

  @Test
  void testRedFromAlphaHexString() {
    String red200 = "#c8ff0000";

    Color redDecoded = ColorHelper.decode(red200);

    Color alphaRed = new Color(255, 0, 0, 200);
    assertEquals(alphaRed.getRed(), redDecoded.getRed());
  }

  @Test
  void testGreenFromAlphaHexString() {
    String red200 = "#c8ff0000";

    Color redDecoded = ColorHelper.decode(red200);

    Color alphaRed = new Color(255, 0, 0, 200);
    assertEquals(alphaRed.getGreen(), redDecoded.getGreen());
  }

  @Test
  void testBlueFromAlphaHexString() {
    String red200 = "#c8ff0000";

    Color redDecoded = ColorHelper.decode(red200);

    Color alphaRed = new Color(255, 0, 0, 200);
    assertEquals(alphaRed.getBlue(), redDecoded.getBlue());
  }

  @Test
  void testAlphaFromAlphaHexString() {
    String red200 = "#c8ff0000";

    Color redDecoded = ColorHelper.decode(red200);

    Color alphaRed = new Color(255, 0, 0, 200);
    assertEquals(alphaRed.getAlpha(), redDecoded.getAlpha());
  }

  @ParameterizedTest
  @MethodSource("getSolidColorFromAlphaHexString")
  void testSolidColorFromAlphaHexString(String color, Boolean isSolid, Color solidColor) {
    Color colorDecoded = ColorHelper.decode(color, isSolid);
    assertEquals(solidColor, colorDecoded);
  }

  private static Stream<Arguments> getSolidColorFromAlphaHexString() {
    return Stream.of(
        Arguments.of("#c8ff0000", true, new Color(228, 0, 0)),
        Arguments.of("#c800ff00", true, new Color(0, 228, 0)),
        Arguments.of("#c80000ff", true, new Color(0, 0, 228)),
        Arguments.of("", true, null),
        Arguments.of(null, true, null));
  }

  @ParameterizedTest
  @MethodSource("getHexStringWithoutHashtag")
  void testHexStringWithoutHashtag(String color, Color expectedColor) {
    Color colorDecoded = ColorHelper.decode(color);
    assertEquals(expectedColor, colorDecoded);
  }

  private static Stream<Arguments> getHexStringWithoutHashtag() {
    return Stream.of(
        Arguments.of("ff0000", Color.RED),
        Arguments.of("00ff00", Color.GREEN),
        Arguments.of("0000ff", Color.BLUE),
        Arguments.of("000", null));
  }

  @Test
  void testEncodeColor() {
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
  void testEncodeAlphaColor() {
    String redEncoded = ColorHelper.encode(new Color(255, 0, 0, 200));
    String greenEncoded = ColorHelper.encode(new Color(0, 255, 0, 200));
    String blueEncoded = ColorHelper.encode(new Color(0, 0, 255, 200));

    assertEquals("#c8ff0000", redEncoded);
    assertEquals("#c800ff00", greenEncoded);
    assertEquals("#c80000ff", blueEncoded);
  }

  @ParameterizedTest(name = "testRgbBounds {0}, colorValue={1}, expectedRgb={2}")
  @CsvSource({
    "Negative, -10, 0",
    "Zero, 0, 0",
    "InRange, 158, 158",
    "Max, 255, 255",
    "OutOfRange, 300, 255"
  })
  void testRgbBounds(String rgbBound, int colorValue, int expectedRgb) {
    // arrange, act
    int actualRgb = ColorHelper.ensureColorValueRange(colorValue);

    // assert
    assertEquals(expectedRgb, actualRgb);
  }

  @Test
  void testPremultiply() {
    Color color = new Color(225, 0, 0);
    assertEquals(new Color(225, 0, 0), ColorHelper.premultiply(color));
  }
}
