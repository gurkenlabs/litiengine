package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.gui.screens.Resolution;

public class ResolutionTests {

  @Test
  public void testCustom(){
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
  public void testDimensionString() {
    // act
    Resolution res = Resolution.Ratio16x9.RES_1280x720;

    // assert
    assertEquals("1280x720", res.toDimensionString());
    assertEquals("1280---720", res.toDimensionString("---"));
  }

  @Test
  public void testToString(){
    // arrange
    Resolution resolution = Resolution.Ratio16x10.RES_1920x1200;

    // act
    String resolutionString = resolution.toString();

    // assert
    assertEquals("1920x1200", resolutionString);
  }

  @Test
  public void testGetDimension(){
    // arrange
    Resolution resolution = Resolution.Ratio4x3.RES_800x600;

    // act
    Dimension dimension = resolution.getDimension();

    // assert
    assertEquals(600, dimension.height);
    assertEquals(800, dimension.width);
  }
}
