package de.gurkenlabs.litiengine.sound.spi.mp3;

public class OverlapAdd {

  private static final int HALF_BLOCK_SIZE = 18;

  private final float[] previousBlock;

  public OverlapAdd() {
    this.previousBlock = new float[HALF_BLOCK_SIZE];
  }

  public float[] process(float[] currentBlock) {
    float[] output = new float[HALF_BLOCK_SIZE];

    if (currentBlock == null || currentBlock.length < 36) {
      return output;
    }

    for (int i = 0; i < HALF_BLOCK_SIZE; i++) {
      output[i] = currentBlock[i] + previousBlock[i];
    }

    System.arraycopy(currentBlock, HALF_BLOCK_SIZE, previousBlock, 0, HALF_BLOCK_SIZE);

    return output;
  }

  public void reset() {
    for (int i = 0; i < HALF_BLOCK_SIZE; i++) {
      previousBlock[i] = 0;
    }
  }

  public float[] getPreviousBlock() {
    return previousBlock;
  }
}
