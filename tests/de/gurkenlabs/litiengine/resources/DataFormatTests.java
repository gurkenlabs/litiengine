package de.gurkenlabs.litiengine.resources;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class DataFormatTests {

  @Test
  public void testImageFormat() {
    assertTrue(ImageFormat.isSupported("test.gif"));
    assertTrue(ImageFormat.isSupported("test.png"));
    assertTrue(ImageFormat.isSupported("test.jpg"));
    assertTrue(ImageFormat.isSupported("test.bmp"));

    assertFalse(ImageFormat.isSupported("test.test"));
    assertFalse(ImageFormat.isSupported("test.undefined"));

    String[] expected = new String[] { "gif", "png", "jpg", "bmp" };

    for (String actual : ImageFormat.getAllExtensions()) {
      assertTrue(Arrays.stream(expected).anyMatch(actual::equals));
    }
  }

  @Test
  public void testAudioFormat() {
    assertTrue(SoundFormat.isSupported("test.ogg"));
    assertTrue(SoundFormat.isSupported("test.mp3"));
    assertTrue(SoundFormat.isSupported("test.wav"));

    assertFalse(SoundFormat.isSupported("test.test"));
    assertFalse(SoundFormat.isSupported("test.undefined"));

    String[] expected = new String[] { "ogg", "mp3", "wav" };

    for (String actual : SoundFormat.getAllExtensions()) {
      assertTrue(Arrays.stream(expected).anyMatch(actual::equals));
    }
  }
}
