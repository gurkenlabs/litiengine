package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.resources.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

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
  void rejectingAnInputStreamRestoresItsPosition() throws Exception {
    byte[] wav = readResource("de/gurkenlabs/litiengine/resources/bop.wav");
    var stream = new ByteArrayInputStream(wav);

    assertThrows(UnsupportedAudioFileException.class,
      () -> new Mp3FileReader().getAudioInputStream(stream));

    assertEquals('R', stream.read());
  }

  @Test
  void doesNotClaimRiffDataContainingAnMpegFrame() throws Exception {
    byte[] mp3 = readSample();
    byte[] riffWithMpegData = new byte[mp3.length + 12];
    System.arraycopy("RIFF....WAVE".getBytes(StandardCharsets.US_ASCII), 0,
      riffWithMpegData, 0, 12);
    System.arraycopy(mp3, 0, riffWithMpegData, 12, mp3.length);

    assertThrows(UnsupportedAudioFileException.class,
      () -> new Mp3FileReader().getAudioFileFormat(new ByteArrayInputStream(riffWithMpegData)));
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

  @Test
  void acceptsId3v24TagsWithFooters() throws Exception {
    byte[] data = readSample();
    byte[] untagged = Arrays.copyOfRange(data, Mpeg.getId3TagLength(data), data.length);
    var tagged = new ByteArrayOutputStream();
    tagged.write(new byte[]{'I', 'D', '3', 4, 0, 0x10, 0, 0, 0, 0});
    tagged.write(new byte[]{'3', 'D', 'I', 4, 0, 0x10, 0, 0, 0, 0});
    tagged.write(untagged);

    var format = new Mp3FileReader().getAudioFileFormat(new ByteArrayInputStream(tagged.toByteArray()));

    assertEquals(32000.0f, format.getFormat().getSampleRate());
  }

  private static byte[] readSample() throws IOException {
    return readResource(MP3_RESOURCE);
  }

  private static byte[] readResource(String resource) throws IOException {
    try (var stream = Resources.getLocation(resource).openStream()) {
      return stream.readAllBytes();
    }
  }
}
