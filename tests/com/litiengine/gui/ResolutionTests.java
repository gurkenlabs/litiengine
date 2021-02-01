package com.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.litiengine.gui.screens.Resolution;

public class ResolutionTests {

  @Test
  public void testDimensionString() {
    Resolution res = Resolution.Ratio16x9.RES_1280x720;

    assertEquals("1280x720", res.toDimensionString());
    assertEquals("1280---720", res.toDimensionString("---"));
  }

  @Test
  public void testCorrectRatio() {
    List<Resolution> resolutions16x9 = Resolution.Ratio16x9.getAll();

    for (Resolution res : resolutions16x9) {
      assertEquals("16:9", res.getRatio().getName());
    }
    
    List<Resolution> resolutions16x10 = Resolution.Ratio16x10.getAll();

    for (Resolution res : resolutions16x10) {
      assertEquals("16:10", res.getRatio().getName());
    }
    
    List<Resolution> resolutions4x3 = Resolution.Ratio4x3.getAll();

    for (Resolution res : resolutions4x3) {
      assertEquals("4:3", res.getRatio().getName());
    }
    
    List<Resolution> resolutions5x4 = Resolution.Ratio5x4.getAll();

    for (Resolution res : resolutions5x4) {
      assertEquals("5:4", res.getRatio().getName());
    }
  }
}
