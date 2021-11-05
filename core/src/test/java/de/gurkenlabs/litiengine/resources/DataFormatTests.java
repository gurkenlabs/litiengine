package de.gurkenlabs.litiengine.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DataFormatTests {

  @ParameterizedTest
  @MethodSource("getImageFormat")
  void testImageFormat(String fileName, boolean assertValue) {

    assertEquals(ImageFormat.isSupported(fileName), assertValue);

    String[] expected = new String[] {"gif", "png", "jpg", "bmp"};

    for (String actual : ImageFormat.getAllExtensions()) {
      assertTrue(Arrays.stream(expected).anyMatch(actual::equals));
    }
  }

  @Test
  void testAudioFormat() {
    assertTrue(SoundFormat.isSupported("test.ogg"));
    assertTrue(SoundFormat.isSupported("test.mp3"));
    assertTrue(SoundFormat.isSupported("test.wav"));

    assertFalse(SoundFormat.isSupported("test.test"));
    assertFalse(SoundFormat.isSupported("test.undefined"));

    String[] expected = new String[] {"ogg", "mp3", "wav"};

    for (String actual : SoundFormat.getAllExtensions()) {
      assertTrue(Arrays.stream(expected).anyMatch(actual::equals));
    }
  }

  private static Stream<Arguments> getImageFormat() {
    return Stream.of(
            Arguments.of("test.gif", true),
            Arguments.of("test.png", true),
            Arguments.of("test.jpg", true),
            Arguments.of("test.bmp", true),
            Arguments.of("test.test", false),
            Arguments.of("test.undefined", false));
  }
}
