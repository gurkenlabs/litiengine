package de.gurkenlabs.litiengine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameTimeTests {
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

  @Test
  public void toMilliseconds_NegativeTicks() {
    // arrange
    GameTime time = new GameTime();

    // act, assert
    assertEquals(-200, time.toMilliseconds(-20, 100));
  }

  @Test
  public void toMilliseconds_NegativeUpdateRate() {
    // arrange
    GameTime time = new GameTime();

    // act, assert
    assertEquals(-400, time.toMilliseconds(1000, -2500));
  }

  @Test
  public void toMilliseconds_ZeroTicks() {
    // arrange
    GameTime time = new GameTime();

    // act, assert
    assertEquals(0, time.toMilliseconds(0, 1000));
  }

  @Test
  public void toMilliseconds_ZeroUpdateRateThrows() {
    // arrange
    GameTime time = new GameTime();

    // act, assert
    assertThrows(ArithmeticException.class, () -> {
      long failed = time.toMilliseconds(100, 0);
    });
  }
}
