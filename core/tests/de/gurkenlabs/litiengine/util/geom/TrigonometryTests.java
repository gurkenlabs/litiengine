package de.gurkenlabs.litiengine.util.geom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TrigonometryTests {

  @ParameterizedTest(name = "{0}: ({1}, {2}) = {3}")
  @CsvSource({
    "'On-point y=0', 0f, 1f, 0",
    "'On-point x=0', 1f, 0f, 1.5707964f",
    "'On-point y=0 x=0', 0f, 0f, 0f",
    "'Off-point y negative x positive', -0.0000001f, 0.0000001f, -0.7853982f",
    "'Off-point y positive x negative', 0.0000001f, -0.0000001f, 2.3561945f",
    "'Off-point y positive x positive', 0.0000001f, 0.0000001f, 0.7853982f",
    "'Off-point y negative x negative', -0.0000001f, -0.0000001f, -2.3561945f",
    "'x negative, y on-point', 0.0f, -0.5f, 3.1415927f"
  })
  public void testATan2AtBoundaries(String boundary, float y, float x, float expectedResult) {
    // act
    float actualResult = Trigonometry.atan2(y, x);

    // assert
    assertEquals(expectedResult, actualResult);
  }

  @Test
  public void testAtan2Deg() {
    // arrange
    float y = 1.0f;
    float x = 1.0f;

    // act
    float result = Trigonometry.atan2Deg(y, x);

    // assert
    assertEquals(45.0, result);
  }

  @Test
  public void testAtan2DegStrict() {
    // arrange
    float y = 1.2f;
    float x = -0.5f;

    // act
    float result = Trigonometry.atan2DegStrict(y, x);

    // assert
    assertEquals(112.61986f, result);
  }

  @Test
  public void testCos() {
    // arrange
    float radians = 4.7874381f;

    // act
    float result = Trigonometry.cos(radians);

    // assert
    assertEquals(0.074329436f, result);
  }

  @Test
  public void testSin() {
    // arrange
    float radians = 1.04719755f;

    // act
    float result = Trigonometry.sin(radians);

    // assert
    assertEquals(0.86589754f, result);
  }

  @Test
  public void testSinDeg() {
    // arrange
    float degrees = 271.03f;

    // act
    float result = Trigonometry.sinDeg(degrees);

    // assert
    assertEquals(-0.9998443722724915, result);
  }
}
