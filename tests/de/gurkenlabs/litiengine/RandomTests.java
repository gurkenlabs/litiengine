package de.gurkenlabs.litiengine;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
  public void nextDouble_OnPoint() { // ==
    // assert
    assertEquals(1.0, Game.random().nextDouble(1.0, 1.0));
  }

  @Test
  public void nextDouble_OffPoint() { // else
    // arrange
    GameRandom random = mock(GameRandom.class);
    when(random.nextDouble()).thenReturn(0.5);
    when(random.nextDouble(anyDouble(), anyDouble())).thenCallRealMethod();

    // act, assert
    assertEquals(1.05, random.nextDouble(1.0, 1.1)); // 1.0 + rand * (1.1 - 1.0)
  }

  @Test
  public void nextDouble_InPoint() { // greater
    // assert
    assertThrows(IllegalArgumentException.class, () -> {
      Game.random().nextDouble(1.1, 1.0);
    });
  }

  @Test
  public void nextDouble_OutPoint() { // else
    // arrange
    GameRandom random = mock(GameRandom.class);
    when(random.nextDouble()).thenReturn(0.5);
    when(random.nextDouble(anyDouble(), anyDouble())).thenCallRealMethod();

    // act, assert
    assertEquals(6.5, random.nextDouble(4.0, 9.0)); // 4.0 + rand * (9.0 - 4.0)
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

    int chosenInt = Game.random().choose(testInt);
    long chosenLong = Game.random().choose(testLong);
    double chosenDouble = Game.random().choose(testDouble);

    Integer chosenWrapper = Game.random().choose(testWrapper);

    assertTrue(Arrays.stream(testInt).anyMatch(x -> x == chosenInt));
    assertTrue(Arrays.stream(testLong).anyMatch(x -> x == chosenLong));
    assertTrue(Arrays.stream(testDouble).anyMatch(x -> x == chosenDouble));
    assertTrue(Arrays.stream(testWrapper).anyMatch(x -> x == chosenWrapper));
  }

  @Test
  public void testCollectionChose() {

    List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);

    Integer chosenInt = Game.random().choose(list);
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
  public void testRandomLocationInCircle() {
    Game.random().setSeed(12345);
    Ellipse2D circle = new Ellipse2D.Double(0, 0, 100, 100);
    for (int i = 0; i < 100; i++) {
      Point2D rnd = Game.random().getLocation(circle);
      assertTrue(rnd.getX() >= 0, rnd.getX() + "should be >= 0");
      assertTrue(rnd.getY() >= 0, rnd.getY() + "should be >= 0");
      
      assertTrue(rnd.getX() <= 100, rnd.getX() + "should be <= 0");
      assertTrue(rnd.getY() <= 100, rnd.getY() + "should be <= 0");
    }
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
