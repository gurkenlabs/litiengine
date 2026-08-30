package de.gurkenlabs.litiengine.sound.spi.mp3;

/** Hybrid synthesis transform used by MPEG-1 Layer III. */
final class Imdct {
  private Imdct() {}

  static float[] process(float[] frequencyData, int blockType, boolean mixedBlock) {
    if (blockType == MpegFrame.SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS && !mixedBlock) {
      return shortTransform(frequencyData);
    }
    return longTransform(frequencyData, blockType);
  }

  static float[] processLongBlock(float[] frequencyData) {
    return longTransform(frequencyData, 0);
  }

  static float[] processShortBlock(float[] frequencyData) {
    return shortTransform(frequencyData);
  }

  private static float[] longTransform(float[] input, int blockType) {
    float[] output = new float[36];
    float[] window = WindowFunctions.getWindow(blockType);
    for (int n = 0; n < output.length; n++) {
      double value = 0;
      for (int k = 0; k < 18; k++) {
        value += input[k] * Math.cos(Math.PI / 72.0 * (2 * n + 19) * (2 * k + 1));
      }
      output[n] = (float) (value * window[n]);
    }
    return output;
  }

  private static float[] shortTransform(float[] input) {
    float[] output = new float[36];
    float[] window = WindowFunctions.getShortWindow();
    for (int windowIndex = 0; windowIndex < 3; windowIndex++) {
      for (int n = 0; n < 12; n++) {
        double value = 0;
        for (int k = 0; k < 6; k++) {
          value += input[3 * k + windowIndex]
            * Math.cos(Math.PI / 24.0 * (2 * n + 7) * (2 * k + 1));
        }
        output[6 + 6 * windowIndex + n] += (float) (value * window[n]);
      }
    }
    return output;
  }

  /** Kept for source compatibility; transforms are already windowed. */
  static float[] applyWindow(float[] data, int blockType) {
    return data.clone();
  }

  static float[] applyWindowShort(float[] data, int windowIndex) {
    float[] result = data.clone();
    float[] window = WindowFunctions.getShortWindow();
    for (int i = 0; i < Math.min(result.length, window.length); i++) result[i] *= window[i];
    return result;
  }
}
