package de.gurkenlabs.litiengine.sound;

import de.gurkenlabs.litiengine.resources.Resources;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SoundTests {
  @Test
  void loadsMp3AsPcm() throws Exception {
    try (var stream = Resources.getLocation("de/gurkenlabs/litiengine/resources/sample.mp3").openStream()) {
      var sound = new Sound(stream, "sample.mp3");

      assertEquals(AudioFormat.Encoding.PCM_SIGNED, sound.getFormat().getEncoding());
      assertEquals(32000.0f, sound.getFormat().getSampleRate());
      assertEquals(1, sound.getFormat().getChannels());
      assertEquals(965376, sound.getStreamData().length);
    }
  }
}
