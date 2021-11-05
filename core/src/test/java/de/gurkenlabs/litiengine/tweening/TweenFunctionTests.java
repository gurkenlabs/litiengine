package de.gurkenlabs.litiengine.tweening;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TweenFunctionTests {

  @Test
  void testGetEquation() {
    // act
    TweenEquation result = TweenFunction.BACK_IN.getEquation();

    // assert
    assertEquals(-0.023127416f, result.compute(0.131f));
  }

  @ParameterizedTest(name = "testTween function={0} input={1} expectedResult={2}")
  @MethodSource("getTweenParameters")
  void testTween(TweenFunction function, float input, float expectedResult) {
    // act
    float actualResult = function.compute(input);

    // assert
    assertEquals(expectedResult, actualResult, 0.000000001f);
  }

  @SuppressWarnings("unused")
  private static Stream<Arguments> getTweenParameters() {
    return Stream.of(
        // linear
        Arguments.of(TweenFunction.LINEAR, 11.2f, 11.2f),

        // quad
        Arguments.of(TweenFunction.QUAD_IN, 12.0f, 144.0f),
        Arguments.of(TweenFunction.QUAD_OUT, 22.7f, -469.89005f),
        Arguments.of(TweenFunction.QUAD_INOUT, 0.793f, 0.914302f), // partition
        Arguments.of(TweenFunction.QUAD_INOUT, 0.5f, 0.5f), // boundary
        Arguments.of(TweenFunction.QUAD_INOUT, 0.2132f, 0.09090848f), // partition

        // circle
        Arguments.of(TweenFunction.CIRCLE_IN, 0.8392f, 0.45617712f),
        Arguments.of(TweenFunction.CIRCLE_OUT, 0.1829f, 0.57649595f),
        Arguments.of(TweenFunction.CIRCLE_INOUT, 0.793f, 0.95513844f), // partition
        Arguments.of(TweenFunction.CIRCLE_INOUT, 0.5f, 0.5f), // boundary
        Arguments.of(TweenFunction.CIRCLE_INOUT, 0.2132f, 0.047732648f), // partition

        // sine
        Arguments.of(TweenFunction.SINE_IN, 0.182f, 0.040587526f),
        Arguments.of(TweenFunction.SINE_OUT, 0.782f, 0.058059696f),
        Arguments.of(TweenFunction.SINE_INOUT, 0.872f, 0.400145f),

        // expo
        Arguments.of(TweenFunction.EXPO_IN, 1.281f, 7.0128465f),
        Arguments.of(TweenFunction.EXPO_OUT, 0.9292f, 0.99840474f),
        Arguments.of(TweenFunction.EXPO_INOUT, 0.4015f, 0.1276265f), // partition
        Arguments.of(TweenFunction.EXPO_INOUT, 0.5f, 0.5f), // boundary
        Arguments.of(TweenFunction.EXPO_INOUT, 0.7351f, 0.98079f), // partition

        // back
        Arguments.of(TweenFunction.BACK_IN, 0.131f, -0.023127416f),
        Arguments.of(TweenFunction.BACK_OUT, 2.156f, 7.447294f),
        Arguments.of(TweenFunction.BACK_INOUT, 0.325f, -0.05454861f), // partition
        Arguments.of(TweenFunction.BACK_INOUT, 0.5f, 0.5f), // boundary
        Arguments.of(TweenFunction.BACK_INOUT, 0.675f, 1.0545486f), // partition

        // bounce
        Arguments.of(TweenFunction.BOUNCE_OUT, 0.1251f, 0.1183532f), // partition
        Arguments.of(TweenFunction.BOUNCE_OUT, 1.0f / 2.75f, 1.0f), // boundary
        Arguments.of(TweenFunction.BOUNCE_OUT, 0.5234f, 0.75367844f), // partition
        Arguments.of(TweenFunction.BOUNCE_OUT, 2.5f * (1.0f / 2.75f), 1.0f), // boundary
        Arguments.of(TweenFunction.BOUNCE_OUT, 0.8953f, 0.9824757f), // partition
        Arguments.of(TweenFunction.BOUNCE_OUT, 0.9928f, 0.99544203f), // partition
        Arguments.of(TweenFunction.BOUNCE_IN, 0.429f, 0.24506497f),
        Arguments.of(TweenFunction.BOUNCE_INOUT, 0.439f, 0.4437199f), // partition
        Arguments.of(TweenFunction.BOUNCE_INOUT, 0.5f, 0.5f), // boundary
        Arguments.of(TweenFunction.BOUNCE_INOUT, 0.7532f, 0.88076735f), // partition

        // elastic
        Arguments.of(TweenFunction.ELASTIC_IN, 0.36846f, 0.009915277f),
        Arguments.of(TweenFunction.ELASTIC_OUT, 0.98435f, 1.0002118f),
        Arguments.of(TweenFunction.ELASTIC_INOUT, -1.262f, 0f), // partition
        Arguments.of(TweenFunction.ELASTIC_INOUT, 0.5f, 0.5f), // boundary
        Arguments.of(TweenFunction.ELASTIC_INOUT, 0.256f, 0.014645687f) // partition
        );
  }
}
