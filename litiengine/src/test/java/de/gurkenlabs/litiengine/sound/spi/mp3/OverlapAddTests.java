package de.gurkenlabs.litiengine.sound.spi.mp3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OverlapAddTests {

  @Test
  void testOverlapAddOutputLength() {
    OverlapAdd overlapAdd = new OverlapAdd();

    float[] currentBlock = new float[36];
    for (int i = 0; i < 36; i++) {
      currentBlock[i] = 1.0f;
    }

    float[] output = overlapAdd.process(currentBlock);

    assertNotNull(output);
    assertEquals(18, output.length, "Output should have 18 samples");
  }

  @Test
  void testOverlapAddFirstBlock() {
    OverlapAdd overlapAdd = new OverlapAdd();

    float[] currentBlock = new float[36];
    for (int i = 0; i < 36; i++) {
      currentBlock[i] = 1.0f;
    }

    float[] output = overlapAdd.process(currentBlock);

    for (int i = 0; i < 18; i++) {
      assertEquals(1.0f, output[i], 0.0001f, "First block output should equal input");
    }
  }

  @Test
  void testOverlapAddSecondBlock() {
    OverlapAdd overlapAdd = new OverlapAdd();

    float[] firstBlock = new float[36];
    for (int i = 0; i < 36; i++) {
      firstBlock[i] = 1.0f;
    }
    overlapAdd.process(firstBlock);

    float[] secondBlock = new float[36];
    for (int i = 0; i < 36; i++) {
      secondBlock[i] = 2.0f;
    }

    float[] output = overlapAdd.process(secondBlock);

    for (int i = 0; i < 18; i++) {
      assertEquals(2.0f + 1.0f, output[i], 0.0001f, "Second block should overlap-add with first");
    }
  }

  @Test
  void testOverlapAddReset() {
    OverlapAdd overlapAdd = new OverlapAdd();

    float[] firstBlock = new float[36];
    for (int i = 0; i < 36; i++) {
      firstBlock[i] = 1.0f;
    }
    overlapAdd.process(firstBlock);

    overlapAdd.reset();

    float[] secondBlock = new float[36];
    for (int i = 0; i < 36; i++) {
      secondBlock[i] = 2.0f;
    }

    float[] output = overlapAdd.process(secondBlock);

    for (int i = 0; i < 18; i++) {
      assertEquals(2.0f, output[i], 0.0001f, "After reset, no overlap-add should occur");
    }
  }
}
