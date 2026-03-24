package de.gurkenlabs.litiengine.sound.spi.mp3;

public class Reordering {

  private static final int[] SCALE_FACTOR_BANDS_SHORT = {0, 4, 8, 12, 16, 22, 30, 40, 52, 66, 84, 106, 136, 192, 576};

  public static float[] reorder(float[] input, int blockType, boolean mixedBlockFlag) {
    if (blockType != MpegFrame.SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS) {
      return input.clone();
    }

    if (mixedBlockFlag) {
      return reorderMixed(input);
    } else {
      return reorderShort(input);
    }
  }

  private static float[] reorderShort(float[] input) {
    float[] output = new float[576];

    int outIndex = 0;

    for (int sfb = 0; sfb < 13; sfb++) {
      int bandStart = SCALE_FACTOR_BANDS_SHORT[sfb];
      int bandEnd = Math.min(SCALE_FACTOR_BANDS_SHORT[sfb + 1], 576);

      for (int window = 0; window < 3; window++) {
        for (int freq = bandStart; freq < bandEnd; freq++) {
          int inIndex = window * 192 + sfb * (bandEnd - bandStart) + (freq - bandStart);
          if (inIndex < input.length && outIndex < output.length) {
            output[outIndex++] = input[inIndex];
          }
        }
      }
    }

    return output;
  }

  private static float[] reorderMixed(float[] input) {
    float[] output = new float[576];

    for (int i = 0; i < 36 && i < input.length; i++) {
      output[i] = input[i];
    }

    int outIndex = 36;
    for (int sfb = 3; sfb < 13; sfb++) {
      int bandStart = SCALE_FACTOR_BANDS_SHORT[sfb];
      int bandEnd = Math.min(SCALE_FACTOR_BANDS_SHORT[sfb + 1], 576);

      for (int window = 0; window < 3; window++) {
        for (int freq = bandStart; freq < bandEnd; freq++) {
          int inIndex = 36 + window * 192 + (sfb - 3) * (bandEnd - bandStart) + (freq - bandStart);
          if (inIndex < input.length && outIndex < output.length) {
            output[outIndex++] = input[inIndex];
          }
        }
      }
    }

    return output;
  }

  public static boolean needsReordering(int blockType) {
    return blockType == MpegFrame.SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS;
  }
}
