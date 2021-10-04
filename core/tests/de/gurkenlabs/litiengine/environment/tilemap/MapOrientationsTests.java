package de.gurkenlabs.litiengine.environment.tilemap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MapOrientationsTests {

  @ParameterizedTest
  @MethodSource("getForNameParameters")
  public void testForName(String orientationName, IMapOrientation expectedOrientation) {
    // act
    IMapOrientation actualOrientation = MapOrientations.forName(orientationName);

    // assert
    assertEquals(expectedOrientation, actualOrientation);
  }

  private static Stream<Arguments> getForNameParameters() {
    // arrange
    return Stream.of(
        Arguments.of("orthogonal", MapOrientations.ORTHOGONAL),
        Arguments.of("isometric", MapOrientations.ISOMETRIC),
        Arguments.of("staggered", MapOrientations.ISOMETRIC_STAGGERED),
        Arguments.of("hexagonal", MapOrientations.HEXAGONAL),
        Arguments.of("somethingInvalid", null));
  }
}
