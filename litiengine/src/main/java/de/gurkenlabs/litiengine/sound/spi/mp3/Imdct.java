package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.sound.spi.mp3.MpegFrame.SideInfo;

public class Imdct {

  private static final int LONG_BLOCK_SIZE = 36;
  private static final int SHORT_BLOCK_SIZE = 12;
  private static final int FREQUENCY_LINES_LONG = 18;
  private static final int FREQUENCY_LINES_SHORT = 6;

  private static final double PI = Math.PI;

  public static float[] process(float[] frequencyData, int blockType, boolean mixedBlockFlag) {
    if (blockType == SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS) {
      return processShortBlock(frequencyData);
    } else {
      return processLongBlock(frequencyData);
    }
  }

  public static float[] processLongBlock(float[] frequencyData) {
    float[] output = new float[LONG_BLOCK_SIZE];
    
    // Check if all input frequency data is zero
    boolean hasNonZero = false;
    for (int k = 0; k < Math.min(FREQUENCY_LINES_LONG, frequencyData.length); k++) {
      if (frequencyData[k] != 0) {
        hasNonZero = true;
        break;
      }
    }
    if (!hasNonZero) {
      // All input frequency data is zero, skip processing
      return output;
    }

    // IMDCT for 18 frequency lines to 36 time-domain samples
    // Formula: x[n] = sum_{k=0}^{17} X[k] * cos(pi/72 * (2n + 1 + 18) * (2k + 1))
    // Simplified: x[n] = sum_{k=0}^{17} X[k] * cos(pi/36 * (n + 9.5) * (2k + 1))
    for (int n = 0; n < LONG_BLOCK_SIZE; n++) {
      double sum = 0;
      for (int k = 0; k < FREQUENCY_LINES_LONG; k++) {
        if (k >= frequencyData.length) break;
        double cos = Math.cos(PI / 36.0 * (n + 9.5) * (2 * k + 1));
        sum += frequencyData[k] * cos;
      }
      output[n] = (float) sum;
    }

    return output;
  }

  public static float[] processShortBlock(float[] frequencyData) {
    float[] output = new float[LONG_BLOCK_SIZE];

    for (int w = 0; w < 3; w++) {
      float[] shortPart = new float[FREQUENCY_LINES_SHORT];
      int offset = w * FREQUENCY_LINES_SHORT;
      if (offset + FREQUENCY_LINES_SHORT <= frequencyData.length) {
        System.arraycopy(frequencyData, offset, shortPart, 0, FREQUENCY_LINES_SHORT);
      }
      float[] shortResult = processShortBlockSingle(shortPart);

      int outOffset = w * SHORT_BLOCK_SIZE;
      for (int i = 0; i < SHORT_BLOCK_SIZE && outOffset + i < LONG_BLOCK_SIZE; i++) {
        output[outOffset + i] = shortResult[i];
      }
    }

    return output;
  }

  private static float[] processShortBlockSingle(float[] frequencyData) {
    float[] output = new float[SHORT_BLOCK_SIZE];

    // Check if all input frequency data is zero
    boolean hasNonZero = false;
    for (int k = 0; k < Math.min(FREQUENCY_LINES_SHORT, frequencyData.length); k++) {
      if (frequencyData[k] != 0) {
        hasNonZero = true;
        break;
      }
    }
    if (!hasNonZero) {
      // All input frequency data is zero, skip processing
      return output;
    }

    // IMDCT for 6 frequency lines to 12 time-domain samples
    // Formula: x[n] = sum_{k=0}^{5} X[k] * cos(pi/12 * (n + 6.5) * (2k + 1))
    for (int n = 0; n < SHORT_BLOCK_SIZE; n++) {
      double sum = 0;
      for (int k = 0; k < FREQUENCY_LINES_SHORT; k++) {
        double cos = Math.cos(PI / 12.0 * (n + 6.5) * (2 * k + 1));
        sum += frequencyData[k] * cos;
      }
      output[n] = (float) sum;
    }

    return output;
  }

  public static float[] getWindow(int blockType) {
    return switch (blockType) {
      case 0, 1, 3 -> WindowFunctions.getLongWindow();
      case 2 -> WindowFunctions.getShortWindow();
      default -> WindowFunctions.getLongWindow();
    };
  }

  public static float[] getWindowShort(int windowIndex) {
    return WindowFunctions.getShortWindow();
  }

  public static float[] applyWindow(float[] data, int blockType) {
    float[] window = getWindow(blockType);
    float[] result = new float[data.length];

    if (blockType == SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS) {
      // For short blocks, data has 36 samples (3 windows of 12 samples each)
      // window has 12 samples, apply to each window
      for (int w = 0; w < 3; w++) {
        int offset = w * 12;
        for (int i = 0; i < 12; i++) {
          result[offset + i] = data[offset + i] * window[i];
        }
      }
    } else {
      // For long blocks, apply window to entire data
      int len = Math.min(data.length, window.length);
      for (int i = 0; i < len; i++) {
        result[i] = data[i] * window[i];
      }
    }

    return result;
  }

  public static float[] applyWindowShort(float[] data, int windowIndex) {
    float[] window = getWindowShort(windowIndex);
    float[] result = new float[data.length];

    for (int i = 0; i < Math.min(data.length, window.length); i++) {
      result[i] = data[i] * window[i];
    }

    return result;
  }
}
