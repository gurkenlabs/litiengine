package de.gurkenlabs.litiengine.sound.spi.mp3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StereoProcessingTests {
  private static final float EPSILON = 0.00001f;

  @Test
  void reconstructsMidSideSamplesOutsideIntensityBands() {
    float[] middle = {1, 4};
    float[] side = {2, 8};
    boolean[] intensity = {false, true};

    StereoProcessing.applyMidSide(middle, side, intensity);

    assertEquals(3 * 0.707106781f, middle[0], EPSILON);
    assertEquals(-0.707106781f, side[0], EPSILON);
    assertEquals(4, middle[1], EPSILON);
    assertEquals(8, side[1], EPSILON);
  }

  @Test
  void distributesIntensityStereoSamplesUsingScalePosition() {
    float[] left = {10, 10};
    float[] right = {0, 0};
    boolean[] intensity = new boolean[2];

    StereoProcessing.applyIntensity(left, right, intensity, 0, 1, 0);
    StereoProcessing.applyIntensity(left, right, intensity, 1, 2, 3);

    assertEquals(0, left[0], EPSILON);
    assertEquals(10, right[0], EPSILON);
    assertEquals(5, left[1], EPSILON);
    assertEquals(5, right[1], EPSILON);
    assertTrue(intensity[0]);
    assertTrue(intensity[1]);
  }

  @Test
  void positionSevenDoesNotEnableIntensityStereo() {
    float[] left = {10};
    float[] right = {2};
    boolean[] intensity = new boolean[1];

    StereoProcessing.applyIntensity(left, right, intensity, 0, 1, 7);

    assertEquals(10, left[0], EPSILON);
    assertEquals(2, right[0], EPSILON);
    assertFalse(intensity[0]);
  }

  @Test
  void shortIntensityUsesTheLastTransmittedScaleFactorAndLeavesTheImplicitBandUntouched() {
    var scaleFactors = new MpegFrame.MainData.ScaleFactors();
    scaleFactors.s[0][10] = 0;
    scaleFactors.s[0][11] = 3;
    float[] left = new float[576];
    float[] right = new float[576];
    boolean[] intensity = new boolean[576];
    java.util.Arrays.fill(left, 10);

    StereoProcessing.applyShortIntensity(32000, false, scaleFactors, left, right, intensity);

    int[] bands = MpegFrame.MainData.shortBands(32000);
    int lastTransmittedBandStart = 3 * bands[11];
    int implicitBandStart = 3 * bands[12];
    assertEquals(0, left[lastTransmittedBandStart], EPSILON);
    assertEquals(10, right[lastTransmittedBandStart], EPSILON);
    assertTrue(intensity[lastTransmittedBandStart]);
    assertEquals(10, left[implicitBandStart], EPSILON);
    assertEquals(0, right[implicitBandStart], EPSILON);
    assertFalse(intensity[implicitBandStart]);
  }
}
