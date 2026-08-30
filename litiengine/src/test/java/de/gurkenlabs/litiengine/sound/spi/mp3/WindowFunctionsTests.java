package de.gurkenlabs.litiengine.sound.spi.mp3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WindowFunctionsTests {

  @Test
  void testLongWindowLength() {
    float[] window = WindowFunctions.getLongWindow();
    assertEquals(36, window.length, "Long window should have 36 samples");
  }

  @Test
  void testShortWindowLength() {
    float[] window = WindowFunctions.getShortWindow();
    assertEquals(12, window.length, "Short window should have 12 samples");
  }

  @Test
  void testStartWindowLength() {
    float[] window = WindowFunctions.getStartWindow();
    assertEquals(36, window.length, "Start window should have 36 samples");
  }

  @Test
  void testEndWindowLength() {
    float[] window = WindowFunctions.getEndWindow();
    assertEquals(36, window.length, "End window should have 36 samples");
  }

  @Test
  void testWindowValuesArePositive() {
    float[] window = WindowFunctions.getLongWindow();
    for (float v : window) {
      assertTrue(v >= 0, "Window values should be non-negative");
    }
  }

  @Test
  void testApplyWindowLength() {
    float[] data = new float[36];
    for (int i = 0; i < 36; i++) {
      data[i] = 1.0f;
    }

    float[] result = WindowFunctions.applyWindow(data, 0);
    assertEquals(36, result.length);
  }

  @Test
  void testWindowForBlockType0() {
    float[] window = WindowFunctions.getWindow(0);
    assertEquals(36, window.length);
  }

  @Test
  void testWindowForBlockType1() {
    float[] window = WindowFunctions.getWindow(1);
    assertEquals(36, window.length);
  }

  @Test
  void testWindowForBlockType2() {
    float[] window = WindowFunctions.getWindow(2);
    assertEquals(12, window.length);
  }

  @Test
  void testWindowForBlockType3() {
    float[] window = WindowFunctions.getWindow(3);
    assertEquals(36, window.length);
  }

  @Test
  void testWindowValuesAreFinite() {
    float[] window = WindowFunctions.getLongWindow();
    for (float v : window) {
      assertTrue(Float.isFinite(v), "Window values should be finite");
    }
  }
}
