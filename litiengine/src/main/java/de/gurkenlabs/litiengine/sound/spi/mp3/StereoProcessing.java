package de.gurkenlabs.litiengine.sound.spi.mp3;

/** Reconstructs MPEG-1 Layer III joint-stereo channels before spectral reordering. */
final class StereoProcessing {
  private static final float SQRT_HALF = 0.707106781f;
  private static final float[] INTENSITY_RATIOS = {
    0.0f, 0.26794919f, 0.57735027f, 1.0f, 1.73205081f, 3.73205081f, 1.0e11f
  };

  private StereoProcessing() {}

  static void process(MpegFrame frame, MpegFrame.MainData.ScaleFactors[][] scaleFactors, float[][][] samples) {
    if (frame.getChannels() != 2 || (!frame.usesIntensityStereo() && !frame.usesMidSideStereo())) return;

    for (int granule = 0; granule < 2; granule++) {
      float[] left = samples[0][granule];
      float[] right = samples[1][granule];
      boolean[] intensityPositions = new boolean[576];

      if (frame.usesIntensityStereo()) {
        var granuleInfo = frame.getSideInfo().channels[0].granules[granule];
        if (granuleInfo.block_type == MpegFrame.SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS) {
          applyShortIntensity(frame.getSampleRate(), granuleInfo.mixed_block_flag,
            scaleFactors[1][granule], left, right, intensityPositions);
        } else {
          applyLongIntensity(frame.getSampleRate(), scaleFactors[1][granule], left, right, intensityPositions, 21);
        }
      }

      if (frame.usesMidSideStereo()) applyMidSide(left, right, intensityPositions);
    }
  }

  static void applyShortIntensity(int sampleRate, boolean mixedBlock,
      MpegFrame.MainData.ScaleFactors scaleFactors, float[] left, float[] right, boolean[] intensityPositions) {
    int[] bands = MpegFrame.MainData.shortBands(sampleRate);
    int firstBand = mixedBlock ? 3 : 0;
    int highestFirstIntensityBand = firstBand;

    for (int window = 0; window < 3; window++) {
      int lastNonZeroBand = firstBand - 1;
      for (int scaleFactorBand = 11; scaleFactorBand >= firstBand; scaleFactorBand--) {
        if (hasNonZeroShortBand(right, bands, scaleFactorBand, window)) {
          lastNonZeroBand = scaleFactorBand;
          break;
        }
      }

      int firstIntensityBand = lastNonZeroBand + 1;
      highestFirstIntensityBand = Math.max(highestFirstIntensityBand, firstIntensityBand);
      for (int scaleFactorBand = firstIntensityBand; scaleFactorBand < 11; scaleFactorBand++) {
        applyShortBand(left, right, intensityPositions, bands, scaleFactorBand, window,
          scaleFactors.s[window][scaleFactorBand]);
      }
      applyShortBand(left, right, intensityPositions, bands, 11, window, scaleFactors.s[window][10]);
    }

    if (mixedBlock && highestFirstIntensityBand <= 3) {
      applyLongIntensity(sampleRate, scaleFactors, left, right, intensityPositions, 8);
    }
  }

  private static boolean hasNonZeroShortBand(float[] samples, int[] bands, int scaleFactorBand, int window) {
    int width = bands[scaleFactorBand + 1] - bands[scaleFactorBand];
    int start = 3 * bands[scaleFactorBand] + window * width;
    for (int index = start; index < start + width; index++) {
      if (samples[index] != 0) return true;
    }
    return false;
  }

  private static void applyShortBand(float[] left, float[] right, boolean[] intensityPositions,
      int[] bands, int scaleFactorBand, int window, int intensityPosition) {
    int width = bands[scaleFactorBand + 1] - bands[scaleFactorBand];
    int start = 3 * bands[scaleFactorBand] + window * width;
    applyIntensity(left, right, intensityPositions, start, start + width, intensityPosition);
  }

  private static void applyLongIntensity(int sampleRate, MpegFrame.MainData.ScaleFactors scaleFactors,
      float[] left, float[] right, boolean[] intensityPositions, int lastScaleFactorBand) {
    int[] bands = MpegFrame.MainData.longBands(sampleRate);
    int lastNonZero = -1;
    int searchEnd = bands[lastScaleFactorBand];
    for (int index = searchEnd - 1; index >= 0; index--) {
      if (right[index] != 0) {
        lastNonZero = index;
        break;
      }
    }

    int firstIntensityBand = 0;
    while (firstIntensityBand < lastScaleFactorBand && bands[firstIntensityBand + 1] <= lastNonZero) {
      firstIntensityBand++;
    }
    if (lastNonZero >= 0) firstIntensityBand++;

    for (int scaleFactorBand = firstIntensityBand; scaleFactorBand < lastScaleFactorBand; scaleFactorBand++) {
      applyIntensity(left, right, intensityPositions, bands[scaleFactorBand], bands[scaleFactorBand + 1],
        scaleFactors.l[scaleFactorBand]);
    }
    if (lastScaleFactorBand == 21) {
      applyIntensity(left, right, intensityPositions, bands[21], 576, scaleFactors.l[20]);
    }
  }

  static void applyMidSide(float[] left, float[] right, boolean[] intensityPositions) {
    for (int index = 0; index < left.length; index++) {
      if (intensityPositions[index]) continue;
      float middle = left[index];
      float side = right[index];
      left[index] = (middle + side) * SQRT_HALF;
      right[index] = (middle - side) * SQRT_HALF;
    }
  }

  static void applyIntensity(float[] left, float[] right, boolean[] intensityPositions,
      int start, int end, int intensityPosition) {
    if (intensityPosition < 0 || intensityPosition >= INTENSITY_RATIOS.length) return;
    float ratio = INTENSITY_RATIOS[intensityPosition];
    for (int index = start; index < end; index++) {
      float rightSample = left[index] / (1 + ratio);
      left[index] = rightSample * ratio;
      right[index] = rightSample;
      intensityPositions[index] = true;
    }
  }
}
