package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.gui.screens.Resolution;
import java.awt.Dimension;
import org.junit.jupiter.api.Test;

class ResolutionTests {

  @Test
  void testCustom() {
    // arrange
    int width = 999;
    int height = 888;
    String resolutionName = "9:8";

    // act
    Resolution resolution = Resolution.custom(width, height, resolutionName);

    // assert
    assertEquals(resolutionName, resolution.getRatio().getName());
  }

  @Test
  void testDimensionString() {
    // act
    Resolution res = Resolution.Ratio16x9.RES_1280x720;

    // assert
    assertEquals("1280x720", res.toDimensionString());
    assertEquals("1280---720", res.toDimensionString("---"));
  }

  @Test
  void testToString() {
    // arrange
    Resolution resolution = Resolution.Ratio16x10.RES_1920x1200;

    // act
    String resolutionString = resolution.toString();

    // assert
    assertEquals("1920x1200", resolutionString);
  }

  @Test
  void testGetDimension() {
    // arrange
    Resolution resolution = Resolution.Ratio4x3.RES_800x600;

    // act
    Dimension dimension = resolution.getDimension();

    // assert
    assertEquals(600, dimension.height);
    assertEquals(800, dimension.width);
  }
}
