package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.gui.screens.Resolution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ResolutionTests {

  @Test
  public void testDimensionString() {
    Resolution res = Resolution.Ratio16x9.RES_1280x720;

    assertEquals("1280x720", res.toDimensionString());
    assertEquals("1280---720", res.toDimensionString("---"));
  }

  @ParameterizedTest
  @MethodSource("getRatioParameters")
  public void testCorrectRatio(List<Resolution> resolutions, String expectedResolution){
    // act assert
    for(Resolution res: resolutions){
      String actualResolution = res.getRatio().getName();
      assertEquals(expectedResolution, actualResolution);
    }
  }

  private static Stream<Arguments> getRatioParameters(){
    // arrange
    return Stream.of(
            Arguments.of(Resolution.Ratio16x9.getAll(), "16:9"),
            Arguments.of(Resolution.Ratio16x10.getAll(), "16:10"),
            Arguments.of(Resolution.Ratio4x3.getAll(), "4:3"),
            Arguments.of(Resolution.Ratio5x4.getAll(), "5:4")
    );
  }
}
