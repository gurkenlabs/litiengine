package de.gurkenlabs.litiengine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameTimeTests {
  @Test
  public void toMilliseconds_Positive() {
    // arrange
    GameTime time = new GameTime();

    // act, assert
    assertEquals(2000, time.toMilliseconds(100, 50));
    assertEquals(13636, time.toMilliseconds(450, 33));
    assertEquals(330, time.toMilliseconds(33, 100));
  }

  @Test
  public void toMilliseconds_Negative() {
    // arrange
    GameTime time = new GameTime();

    // act, assert
    assertEquals(-200, time.toMilliseconds(-20, 100));
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
