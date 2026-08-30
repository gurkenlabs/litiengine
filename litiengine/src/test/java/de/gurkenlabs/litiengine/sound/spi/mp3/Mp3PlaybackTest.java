package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.resources.Resources;
import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Manual speaker test. Set LITIENGINE_TEST_AUDIO_PLAYBACK=true to enable it. */
class Mp3PlaybackTest {
  private static final long EXPECTED_PCM_BYTES = 963072;

  @Test
  void playsDecodedMp3ThroughTheDefaultAudioDevice() throws Exception {
    Assumptions.assumeTrue(Boolean.parseBoolean(System.getenv("LITIENGINE_TEST_AUDIO_PLAYBACK")),
      "Set LITIENGINE_TEST_AUDIO_PLAYBACK=true to hear the MP3 playback test");

    byte[] mp3;
    try (var input = Resources.getLocation("de/gurkenlabs/litiengine/resources/sample.mp3").openStream()) {
      mp3 = input.readAllBytes();
    }

    long bytesPlayed = 0;
    try (var source = new Mp3FileReader().getAudioInputStream(new ByteArrayInputStream(mp3))) {
      var format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, source.getFormat().getSampleRate(), 16,
        source.getFormat().getChannels(), source.getFormat().getChannels() * 2, source.getFormat().getSampleRate(), false);
      try (var decoded = new Mp3FormatConversionProvider().getAudioInputStream(format, source)) {
        var line = AudioSystem.getSourceDataLine(format);
        try {
          line.open(format);
          line.start();
          byte[] buffer = new byte[8192];
          int read;
          while ((read = decoded.read(buffer)) != -1) {
            bytesPlayed += line.write(buffer, 0, read);
          }
          line.drain();
        } finally {
          line.stop();
          line.close();
        }
      }
    }

    assertEquals(EXPECTED_PCM_BYTES, bytesPlayed);
  }
}
