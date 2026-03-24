package de.gurkenlabs.litiengine.sound.spi.mp3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImdctTests {

  @Test
  void testImdctLongBlockOutputLength() {
    float[] input = new float[18];
    for (int i = 0; i < 18; i++) {
      input[i] = 1.0f;
    }

    float[] output = Imdct.processLongBlock(input);

    assertNotNull(output);
    assertEquals(36, output.length, "Long block IMDCT should output 36 samples");
  }

  @Test
  void testImdctShortBlockOutputLength() {
    float[] input = new float[18];
    for (int i = 0; i < 18; i++) {
      input[i] = 1.0f;
    }

    float[] output = Imdct.process(input, 2, false);

    assertNotNull(output);
    assertEquals(36, output.length, "Short block IMDCT should output 36 samples total (3 windows × 12 samples)");
  }

  @Test
  void testImdctWithZeros() {
    float[] input = new float[18];

    float[] output = Imdct.processLongBlock(input);

    assertNotNull(output);
    assertEquals(36, output.length);
  }

  @Test
  void testImdctWithRealValues() {
    float[] input = {1.0f, 0.5f, 0.25f, 0.125f, 0.1f, 0.05f, 0.025f, 0.0125f,
                      0.1f, 0.05f, 0.025f, 0.0125f, 0.1f, 0.05f, 0.025f, 0.0125f, 0.1f, 0.05f};

    float[] output = Imdct.processLongBlock(input);

    assertNotNull(output);
    assertEquals(36, output.length);

    for (float v : output) {
      assertTrue(Float.isFinite(v), "Output should be finite");
    }
  }

  @Test
  void testProcessWithBlockType0() {
    float[] input = new float[18];
    input[0] = 1.0f;

    float[] output = Imdct.process(input, 0, false);

    assertNotNull(output);
    assertEquals(36, output.length);
  }

  @Test
  void testProcessWithBlockType2() {
    float[] input = new float[18];
    input[0] = 1.0f;

    float[] output = Imdct.process(input, 2, false);

    assertNotNull(output);
    assertEquals(36, output.length);
  }

  @Test
  void testApplyWindowLongBlock() {
    float[] data = new float[36];
    for (int i = 0; i < 36; i++) {
      data[i] = 1.0f;
    }

    float[] output = Imdct.applyWindow(data, 0);

    assertNotNull(output);
    assertEquals(36, output.length);
  }

  @Test
  void testApplyWindowShortBlock() {
    float[] data = new float[12];
    for (int i = 0; i < 12; i++) {
      data[i] = 1.0f;
    }

    float[] output = Imdct.applyWindowShort(data, 0);

    assertNotNull(output);
    assertEquals(12, output.length);
  }

  @Test
  void testImdctNoNaNOrInfinity() {
    float[] input = new float[18];
    for (int i = 0; i < 18; i++) {
      input[i] = (float) Math.sin(i * 0.1);
    }

    float[] output = Imdct.processLongBlock(input);

    for (int i = 0; i < output.length; i++) {
      assertFalse(Float.isNaN(output[i]), "Output should not be NaN at index " + i);
      assertFalse(Float.isInfinite(output[i]), "Output should not be Infinite at index " + i);
    }
  }
}
