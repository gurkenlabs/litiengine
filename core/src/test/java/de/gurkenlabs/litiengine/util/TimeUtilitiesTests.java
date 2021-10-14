package de.gurkenlabs.litiengine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.util.TimeUtilities.TimerFormat;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

public class TimeUtilitiesTests {

  @Test
  public void testMillisecondCalculations() {
    long seconds = TimeUtilities.getSeconds(999);
    long minutes = TimeUtilities.getMinutes(59999);
    long hours = TimeUtilities.getHours(3599999);
    long days = TimeUtilities.getDays(86399999);
    assertEquals(0, seconds);
    assertEquals(0, minutes);
    assertEquals(0, hours);
    assertEquals(0, days);

    long seconds2 = TimeUtilities.getSeconds(1000);
    long minutes2 = TimeUtilities.getMinutes(60000);
    long hours2 = TimeUtilities.getHours(3600000);
    long days2 = TimeUtilities.getDays(86400000);
    assertEquals(1, seconds2);
    assertEquals(1, minutes2);
    assertEquals(1, hours2);
    assertEquals(1, days2);
  }

  @ParameterizedTest(name = "testGetRemainingDays time={0} expectedRemainingDays={1}")
  @CsvSource({"3600123, 0", "999991239, 11", "-100000000, -1", "0, 0"})
  public void testGetRemainingDays(long time, long expectedRemainingDays) {
    // act
    long actualRemainingDays = TimeUtilities.getRemainingDays(time);

    // assert
    assertEquals(expectedRemainingDays, actualRemainingDays);
  }

  @ParameterizedTest(name = "testToTimerFormat duration={0} format={1} expectedTime={2}")
  @MethodSource("getToTimerFormatArguments")
  public void testToTimerFormat(long duration, TimerFormat format, String expectedTime) {
    // act
    String actualTime = TimeUtilities.toTimerFormat(duration, format);

    // assert
    assertEquals(expectedTime, actualTime);
  }

  /**
   * Used for @see{@link de.gurkenlabs.litiengine.util.TimeUtilities::testToTimerFormat} Suppression
   * of unused warning is added because usage through MethodSource is not detected
   *
   * @return Input arguments for the unit test
   */
  @SuppressWarnings("unused")
  private static Stream<Arguments> getToTimerFormatArguments() {
    // arrange,
    return Stream.of(
        Arguments.of(3600123l, TimerFormat.HH_MM_SS, "01:00:00"),
        Arguments.of(3600123l, TimerFormat.HH_MM_SS_000, "01:00:00.123"),
        Arguments.of(3599123l, TimerFormat.MM_SS_000, "59:59.123"),
        Arguments.of(3600123l, TimerFormat.HH_MM_SS_0, "01:00:00.1"),
        Arguments.of(3599123l, TimerFormat.MM_SS_0, "59:59.1"),
        Arguments.of(3599123l, TimerFormat.SS_000, "59.123"),
        Arguments.of(3599123l, TimerFormat.S_000, "59.123"),
        Arguments.of(3599123l, TimerFormat.SS_00, "59.12"),
        Arguments.of(3599123l, TimerFormat.S_00, "59.12"),
        Arguments.of(3599123l, TimerFormat.SS_0, "59.1"),
        Arguments.of(3599123l, TimerFormat.S_0, "59.1"),
        Arguments.of(3599123l, TimerFormat.UNDEFINED, "123"));
  }
}
