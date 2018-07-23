package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class GameLoopTest {
  @Test
  public void testTimeCalculation100() {
    final int updateRate = 100;

    try (final GameLoop loop = new GameLoop("Test Loop", updateRate)) {
      assertEquals(1000, loop.convertToMs(100));
      assertEquals(500, loop.convertToMs(50));
      assertEquals(2000, loop.convertToMs(200));
      assertEquals(4500, loop.convertToMs(450));
      assertEquals(330, loop.convertToMs(33));
    }
  }

  @Test
  public void testTimeCalculation50() {
    final int updateRate = 50;

    try (final GameLoop loop = new GameLoop("Test Loop", updateRate)) {
      assertEquals(2000, loop.convertToMs(100));
      assertEquals(1000, loop.convertToMs(50));
      assertEquals(4000, loop.convertToMs(200));
      assertEquals(9000, loop.convertToMs(450));
      assertEquals(660, loop.convertToMs(33));
    }
  }

  @Test
  public void testTimeCalculation33() {
    final int updateRate = 33;

    try (final GameLoop loop = new GameLoop("Test Loop", updateRate)) {
      assertEquals(3030, loop.convertToMs(100));
      assertEquals(1515, loop.convertToMs(50));
      assertEquals(6060, loop.convertToMs(200));
      assertEquals(13636, loop.convertToMs(450));
      assertEquals(1000, loop.convertToMs(33));
    }
  }
}
