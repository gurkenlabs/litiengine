package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class GameLoopTest {

  @Test
  public void testTimeCalculation100() {
    final int updateRate = 100;

    GameTime time = new GameTime();
    assertEquals(1000, time.toMilliseconds(100, updateRate));
    assertEquals(500, time.toMilliseconds(50, updateRate));
    assertEquals(2000, time.toMilliseconds(200, updateRate));
    assertEquals(4500, time.toMilliseconds(450, updateRate));
    assertEquals(330, time.toMilliseconds(33, updateRate));
  }

  @Test
  public void testTimeCalculation50() {
    final int updateRate = 50;

    GameTime time = new GameTime();
    assertEquals(2000, time.toMilliseconds(100, updateRate));
    assertEquals(1000, time.toMilliseconds(50, updateRate));
    assertEquals(4000, time.toMilliseconds(200, updateRate));
    assertEquals(9000, time.toMilliseconds(450, updateRate));
    assertEquals(660, time.toMilliseconds(33, updateRate));

  }

  @Test
  public void testTimeCalculation33() {
    final int updateRate = 33;

    GameTime time = new GameTime();
    assertEquals(3030, time.toMilliseconds(100, updateRate));
    assertEquals(1515, time.toMilliseconds(50, updateRate));
    assertEquals(6060, time.toMilliseconds(200, updateRate));
    assertEquals(13636, time.toMilliseconds(450, updateRate));
    assertEquals(1000, time.toMilliseconds(33, updateRate));
  }
}
