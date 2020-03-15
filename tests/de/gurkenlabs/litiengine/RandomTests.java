package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

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

  @Test
  public void testArrayChose() {
    int[] testInt = new int[] { 1, 2, 3, 4, 5, 6 };
    long[] testLong = new long[] { 1, 2, 3, 4, 5, 6 };
    double[] testDouble = new double[] { 1, 2, 3, 4, 5, 6 };

    Integer[] testWrapper = new Integer[] { 1, 2, 3 };

    int chosenInt = Game.random().chose(testInt);
    long chosenLong = Game.random().chose(testLong);
    double chosenDouble = Game.random().chose(testDouble);

    Integer chosenWrapper = Game.random().chose(testWrapper);

    assertTrue(Arrays.stream(testInt).anyMatch(x -> x == chosenInt));
    assertTrue(Arrays.stream(testLong).anyMatch(x -> x == chosenLong));
    assertTrue(Arrays.stream(testDouble).anyMatch(x -> x == chosenDouble));
    assertTrue(Arrays.stream(testWrapper).anyMatch(x -> x == chosenWrapper));
  }

  @Test
  public void testCollectionChose() {

    List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);

    Integer chosenInt = Game.random().chose(list);
    assertTrue(list.contains(chosenInt));
  }

  @Test
  public void testArrayShuffle() {
    int[] testInt = new int[] { 1, 2, 3, 4, 5, 6 };
    long[] testLong = new long[] { 1, 2, 3, 4, 5, 6 };
    double[] testDouble = new double[] { 1, 2, 3, 4, 5, 6 };

    Integer[] testWrapper = new Integer[] { 1, 2, 3, 4, 5, 6 };

    // set seed to make results reproducible
    Game.random().setSeed(12345);

    Game.random().shuffle(testInt);
    Game.random().shuffle(testLong);
    Game.random().shuffle(testDouble);
    Game.random().shuffle(testWrapper);

    assertArrayEquals(new int[] { 3, 1, 4, 2, 6, 5 }, testInt);
    assertArrayEquals(new long[] { 5, 3, 2, 4, 6, 1 }, testLong);
    assertArrayEquals(new double[] { 5.0, 4.0, 3.0, 2.0, 6.0, 1.0 }, testDouble);
    assertArrayEquals(new Integer[] { 3, 2, 6, 4, 1, 5 }, testWrapper);
  }

  @Test
  public void testGetIndex() {
    double[] probabilities = new double[] { .5, .25, .125, .125 };

    // set seed to make results reproducible
    Game.random().setSeed(1222225);

    int index = Game.random().getIndex(probabilities);
    
    assertEquals(1, index);
  }
}
