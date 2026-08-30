package de.gurkenlabs.litiengine.sound.spi.mp3;

final class Reordering {
  private Reordering() {}

  static float[] reorder(float[] input, int blockType, boolean mixedBlock) {
    return reorder(input, blockType, mixedBlock, 44100);
  }

  static float[] reorder(float[] input, int blockType, boolean mixedBlock, int sampleRate) {
    if (blockType != MpegFrame.SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS) return input.clone();

    int[] bands = MpegFrame.MainData.shortBands(sampleRate);
    float[] output = new float[576];
    int cursor = 0;
    int firstBand = 0;
    if (mixedBlock) {
      System.arraycopy(input, 0, output, 0, 36);
      cursor = 36;
      firstBand = 3;
    }

    for (int sfb = firstBand; sfb < bands.length - 1; sfb++) {
      int width = bands[sfb + 1] - bands[sfb];
      for (int window = 0; window < 3; window++) {
        for (int line = 0; line < width && cursor < input.length; line++) {
          output[3 * (bands[sfb] + line) + window] = input[cursor++];
        }
      }
    }
    return output;
  }

  static boolean needsReordering(int blockType) {
    return blockType == MpegFrame.SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS;
  }
}
