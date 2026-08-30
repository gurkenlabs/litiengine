package de.gurkenlabs.litiengine.sound.spi.mp3;

public class AliasReduction {

  static final double[] CS = {
      0.857492925712, 0.881741997318, 0.949628649103, 0.983314592492,
      0.995517816065, 0.999160558175, 0.999899195243, 0.999993155067
  };

  static final double[] CA = {
      -0.514495755427, -0.471731968565, -0.313377454204, -0.181913199611,
      -0.0945741925262, -0.0409655828852, -0.0141985685725, -0.00369997467375
  };

  public static float[] process(float[] input, int blockType) {
    return process(input, blockType, false);
  }

  public static float[] process(float[] input, int blockType, boolean mixedBlock) {
    if (blockType == MpegFrame.SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS && !mixedBlock) {
      return input.clone();
    }

    float[] output = input.clone();

    int boundaries = blockType == MpegFrame.SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS ? 1 : 31;
    for (int sb = 0; sb < boundaries; sb++) {
      int k = sb * 18;

      for (int i = 0; i < 8; i++) {
        int idx1 = k + 17 - i;
        int idx2 = k + 18 + i;

        if (idx1 < output.length && idx2 < output.length) {
          double bu = output[idx1];
          double bd = output[idx2];

          output[idx1] = (float) (bu * CS[i] - bd * CA[i]);
          output[idx2] = (float) (bu * CA[i] + bd * CS[i]);
        }
      }
    }

    return output;
  }

  public static boolean needsAliasReduction(int blockType) {
    return blockType != MpegFrame.SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS;
  }
}
