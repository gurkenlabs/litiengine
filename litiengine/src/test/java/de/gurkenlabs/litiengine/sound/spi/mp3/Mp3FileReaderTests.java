package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.resources.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Mp3FileReaderTests {
  private static final String MP3_RESOURCE = "de/gurkenlabs/litiengine/resources/sample.mp3";

  @Test
  void invalidFilesAreRejected() {
    var reader = new Mp3FileReader();

    assertThrows(UnsupportedAudioFileException.class,
      () -> reader.getAudioFileFormat(Resources.getLocation("de/gurkenlabs/litiengine/resources/bop.wav").openStream()));
  }

  @Test
  void readsMpegMetadata() throws Exception {
    var format = new Mp3FileReader().getAudioFileFormat(new ByteArrayInputStream(readSample()));

    assertEquals(Mp3FileReader.MP3, format.getType());
    assertEquals(32000.0f, format.getFormat().getSampleRate());
    assertEquals(1, format.getFormat().getChannels());
    assertEquals(AudioSystem.NOT_SPECIFIED, format.getFrameLength());
  }

  @Test
  void acceptsStreamsWithoutId3Tags() throws Exception {
    byte[] data = readSample();
    int tagSize = 10 + ((data[6] & 0x7f) << 21) + ((data[7] & 0x7f) << 14)
      + ((data[8] & 0x7f) << 7) + (data[9] & 0x7f);

    var format = new Mp3FileReader().getAudioFileFormat(
      new ByteArrayInputStream(data, tagSize, data.length - tagSize));

    assertEquals(32000.0f, format.getFormat().getSampleRate());
  }

  @Test
  void fileAndUrlStreamsRemainReadable(@TempDir Path directory) throws Exception {
    Path file = Files.write(directory.resolve("sample.mp3"), readSample());
    var reader = new Mp3FileReader();

    try (var fileStream = reader.getAudioInputStream(file.toFile());
      var urlStream = reader.getAudioInputStream(file.toUri().toURL())) {
      assertEquals(0x49, fileStream.read());
      assertEquals(0x49, urlStream.read());
    }
  }

  private static byte[] readSample() throws IOException {
    try (var stream = Resources.getLocation(MP3_RESOURCE).openStream()) {
      return stream.readAllBytes();
    }
  }
}
