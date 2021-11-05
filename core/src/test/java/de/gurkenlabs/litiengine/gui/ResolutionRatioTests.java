package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.gui.screens.Resolution;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ResolutionRatioTests {

  @ParameterizedTest(name = "testGetAll expectedResolution={1}")
  @MethodSource("getRatioParameters")
  void testGetAll(List<Resolution> resolutions, String expectedResolution) {
    // assert
    assertTrue(
        resolutions.stream().allMatch(x -> x.getRatio().getName().equals(expectedResolution)));
  }

  @ParameterizedTest(name = "testGetAllNotEmpty")
  @MethodSource("getRatioParameters")
  void testGetAllNotEmpty(List<Resolution> resolutions) {
    // assert
    assertNotEquals(0, resolutions.size());
  }

  @Test
  void testGetX() {
    // arrange
    Resolution.Ratio ratio = Resolution.Ratio16x10.RES_1280x800.getRatio();

    // act
    int x = ratio.getX();

    // assert
    assertEquals(16, x);
  }

  @Test
  void testGetY() {
    // arrange
    Resolution.Ratio ratio = Resolution.Ratio4x3.RES_640x480.getRatio();

    // act
    int y = ratio.getY();

    // assert
    assertEquals(3, y);
  }

  @Test
  void testToString() {
    // arrange
    Resolution.Ratio ratio = Resolution.Ratio5x4.RES_1280x1024.getRatio();

    // act
    String result = ratio.toString();

    // assert
    assertEquals("5:4", result);
  }

  @SuppressWarnings("unused")
  private static Stream<Arguments> getRatioParameters() {
    // arrange, act
    return Stream.of(
            Arguments.of(Resolution.Ratio16x9.getAll(), "16:9"),
            Arguments.of(Resolution.Ratio16x10.getAll(), "16:10"),
            Arguments.of(Resolution.Ratio4x3.getAll(), "4:3"),
            Arguments.of(Resolution.Ratio5x4.getAll(), "5:4"));
  }
}
