package de.gurkenlabs.litiengine.sound.spi.mp3;

public class AliasReduction {

  static final double[] CS = {
      -0.0000000000f, -0.0004423847f, -0.0000152588f,  0.0036921507f,  0.0000152588f, -0.0179748535f,
      -0.0000152588f,  0.0649604797f,  0.0000152588f, -0.1261685944f, -0.0000152588f,  0.1829223633f,
       0.0000152588f, -0.2253379821f, -0.0000152588f,  0.2544477404f,  0.0000152588f, -0.2483820915f,
      -0.0000152588f,  0.1923301220f,  0.0000152588f, -0.1280487634f, -0.0000152588f,  0.0676026349f,
      0.0000152588f, -0.0277600288f, -0.0000152588f
  };

  static final double[] CA = {
       0.0000000000f,  0.0004424781f, -0.0000152588f, -0.0036909434f,  0.0000152588f,  0.0179748535f,
      -0.0000152588f, -0.0649604797f,  0.0000152588f,  0.1261685944f, -0.0000152588f, -0.1829223633f,
       0.0000152588f,  0.2253379821f, -0.0000152588f, -0.2544477404f,  0.0000152588f,  0.2483820915f,
      -0.0000152588f, -0.1923301220f,  0.0000152588f,  0.1280487634f, -0.0000152588f, -0.0676026349f,
       0.0000152588f,  0.0277600288f, -0.0000152588f
  };

  public static float[] process(float[] input, int blockType) {
    if (blockType == MpegFrame.SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS) {
      return input.clone();
    }

    float[] output = input.clone();

    for (int sb = 0; sb < 32 - 1; sb++) {
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
