package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RandomTests {
  @Test
  public void testRandomInRange() {
    for (int i = 0; i < 100; i++) {
      double rnd = Game.random().nextDouble(0.0, 10);
      int rndInt = Game.random().nextInt(0, 10);

      assertTrue(rnd >= 0 && rnd < 10);
      assertTrue(rndInt >= 0 && rndInt < 10);
    }
  }

  @Test
  public void testRandomSign() {
    for (int i = 0; i < 100; i++) {
      int rnd = Game.random().nextSign();
      assertTrue(rnd == 1 || rnd == -1);
    }
  }

  @Test
  public void testSeed() {
    Game.random().setSeed("myseed");

    int val1 = Game.random().nextInt();
    char val2 = Game.random().nextChar();
    double val3 = Game.random().nextDouble();
    long val4 = Game.random().nextLong();
    float val5 = Game.random().nextFloat();

    Game.random().setSeed("myseed");

    assertEquals(val1, Game.random().nextInt());
    assertEquals(val2, Game.random().nextChar());
    assertEquals(val3, Game.random().nextDouble());
    assertEquals(val4, Game.random().nextLong());
    assertEquals(val5, Game.random().nextFloat());
  }

  @Test
  public void testAlphaNumeric() {
    for (int i = 0; i < 10; i++) {
      assertTrue(Game.random().nextAlphanumeric(i).length() == i);
    }
  }

  @Test
  public void testAlphabetic() {
    for (int i = 0; i < 10; i++) {
      assertTrue(Game.random().nextAlphabetic(i).length() == i);
      assertTrue(Game.random().nextAlphabetic(i, true).length() == i);
    }
  }

  @Test
  public void testAsciiStrings() {
    for (int i = 0; i < 10; i++) {
      assertTrue(Game.random().nextAscii(i).length() == i);
    }
  }
}
