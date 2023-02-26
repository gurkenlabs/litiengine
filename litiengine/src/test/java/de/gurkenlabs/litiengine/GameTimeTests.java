package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.gurkenlabs.litiengine.test.GameTestSuite;

@ExtendWith(GameTestSuite.class)
class GameTimeTests {
  @ParameterizedTest(name = "toMilliseconds_Positive ticks={0}, updateRate={1}, expected={2}")
  @CsvSource({"100, 50, 2000", "450, 33, 13636", "33, 100, 330"})
  void toMilliseconds_Positive(long ticks, int updateRate, long expected) {
    // arrange
    GameTime time = new GameTime();

    // act, assert
    assertEquals(expected, time.toMilliseconds(ticks, updateRate));
  }

  @Test
  void toMilliseconds_Negative() {
    // arrange
    GameTime time = new GameTime();

    // act, assert
    assertEquals(-200, time.toMilliseconds(-20, 100));
    assertEquals(-400, time.toMilliseconds(1000, -2500));

    assertEquals(200, time.toMilliseconds(-100, -500));
  }

  @Test
  void toMilliseconds_ZeroTicks() {
    // arrange
    GameTime time = new GameTime();

    // act, assert
    assertEquals(0, time.toMilliseconds(0, 1000));
  }

  @Test
  void toMilliseconds_ZeroUpdateRateThrows() {
    // arrange
    GameTime time = new GameTime();

    // act, assert
    assertThrows(
        ArithmeticException.class,
        () -> {
          long failed = time.toMilliseconds(100, 0);
        });
  }
}
