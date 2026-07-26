package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StatusBarTest {
  @Test
  void formatsCurrentAndEstimatedMaximumFps() {
    assertEquals("60 FPS  |  240 MAX", StatusBar.formatFps(60, 240));
  }

  @Test
  void colorsFpsByDistanceFromConfiguredCap() {
    assertEquals(de.gurkenlabs.utiliti.model.Style.COLOR_GREEN, StatusBar.fpsColor(60, 60));
    assertEquals(de.gurkenlabs.utiliti.model.Style.COLOR_GREEN, StatusBar.fpsColor(59, 60));
    assertEquals(de.gurkenlabs.utiliti.model.Style.COLOR_GREEN, StatusBar.fpsColor(61, 60));
    assertEquals(StatusBar.FPS_WARNING_COLOR, StatusBar.fpsColor(58, 60));
    assertEquals(StatusBar.FPS_WARNING_COLOR, StatusBar.fpsColor(54, 60));
    assertEquals(de.gurkenlabs.utiliti.model.Style.COLOR_RED, StatusBar.fpsColor(53, 60));
  }
}
