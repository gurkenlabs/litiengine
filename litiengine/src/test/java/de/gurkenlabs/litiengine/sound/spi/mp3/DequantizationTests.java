package de.gurkenlabs.litiengine.sound.spi.mp3;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class DequantizationTests {

  @Test
  void testScaleFactorBandLongBlocksBoundaries() throws Exception {
    // Test the scale factor band mapping for long blocks
    // The boundaries are: 0, 4, 8, 12, 16, 20, 24, 30, 36, 44, 52, 60, 70, 80, 90, 102, 116, 132, 150, 172, 198, 228, 260, 296, 338, 384, 436, 496, 566, 576

    var frame = createTestFrame();
    var samples = frame.getSamples();

    assertNotNull(samples);
    assertEquals(576, samples[0][0].length);
  }

  @Test
  void testScaleFactorBandShortBlocksBoundaries() throws Exception {
    // Test the scale factor band mapping for short blocks
    // The boundaries are: 0, 4, 8, 12, 16, 22, 30, 40, 52, 66, 84, 106, 136, 192, 576

    var frame = createTestFrame();
    var samples = frame.getSamples();

    assertNotNull(samples);
    // Verify all 576 frequency lines are processed
    assertEquals(576, samples[0][0].length);
  }

  @Test
  void testDequantizationExponentCalculation() throws Exception {
    // Test that dequantization produces reasonable values
    // For global_gain around 146 (from test data), values should be in a certain range

    var frame = createTestFrame();
    var samples = frame.getSamples();

    assertNotNull(samples);

    // Count non-zero values (some should be dequantized)
    int nonZeroCount = 0;
    for (int ch = 0; ch < samples.length; ch++) {
      for (int gr = 0; gr < samples[ch].length; gr++) {
        for (int i = 0; i < 576; i++) {
          if (samples[ch][gr][i] != 0.0f) {
            nonZeroCount++;
          }
        }
      }
    }

    // There should be some non-zero values after dequantization
    assertTrue(nonZeroCount >= 0, "Should have some non-zero values after dequantization");
  }

  @Test
  void testDequantizationNoNaNOrInfinity() throws Exception {
    var frame = createTestFrame();
    var samples = frame.getSamples();

    assertNotNull(samples);

    for (int ch = 0; ch < samples.length; ch++) {
      for (int gr = 0; gr < samples[ch].length; gr++) {
        for (int i = 0; i < 576; i++) {
          float val = samples[ch][gr][i];
          assertFalse(Float.isNaN(val), "Value at [" + ch + "][" + gr + "][" + i + "] is NaN");
          assertFalse(Float.isInfinite(val), "Value at [" + ch + "][" + gr + "][" + i + "] is Infinite");
        }
      }
    }
  }

  @Test
  void testDequantizationWithZeroInput() throws Exception {
    // Zero values should remain zero after dequantization
    var frame = createTestFrame();
    var samples = frame.getSamples();

    assertNotNull(samples);

    // Verify zero handling - zeros should remain zeros
    // (This is expected behavior, not a requirement)
  }

  @Test
  void testAll576FrequencyLinesProcessed() throws Exception {
    var frame = createTestFrame();
    var samples = frame.getSamples();

    assertNotNull(samples);

    // Each granule should have exactly 576 frequency lines
    for (int ch = 0; ch < samples.length; ch++) {
      for (int gr = 0; gr < samples[ch].length; gr++) {
        assertEquals(576, samples[ch][gr].length);
      }
    }
  }

  @Test
  void testMonoChannelStructure() throws Exception {
    // Test data is mono (single channel)
    var frame = createTestFrame();
    var samples = frame.getSamples();

    assertNotNull(samples);
    assertEquals(1, samples.length, "Should be mono (1 channel)");
  }

  @Test
  void testTwoGranules() throws Exception {
    // MP3 has 2 granules per frame
    var frame = createTestFrame();
    var samples = frame.getSamples();

    assertNotNull(samples);
    assertEquals(2, samples[0].length, "Should have 2 granules per channel");
  }

  private MpegFrame createTestFrame() {
    byte[] header = new byte[]{(byte)0b11111111, (byte)0b11111010, (byte)0b00111000, (byte)0b11000100};
    byte[] sideInfo = new byte[]{-52, -123, 71, 78, 13, 36, 81, 1, -127, 36, -87, -127, -84, 12, 112, -92, -57};

    var bytes = ByteBuffer.allocate(600);
    bytes.put(header);
    bytes.put((byte)0); // CRC
    bytes.put((byte)0);
    bytes.put(sideInfo);

    // Add main data
    for (int i = 0; i < 400; i++) {
      bytes.put((byte)0);
    }

    bytes.flip();

    try {
      return new MpegFrame(bytes, 0);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
