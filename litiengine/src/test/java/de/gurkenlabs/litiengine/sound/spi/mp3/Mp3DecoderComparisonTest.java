package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.resources.Resources;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Mp3DecoderComparisonTest {
  private static final String MP3_RESOURCE = "de/gurkenlabs/litiengine/resources/sample.mp3";

  @Test
  void decodedPcmMatchesGoldenOutput() throws Exception {
    byte[] pcm = decodeWithLitiengine(readSample(), false);

    assertEquals(965376, pcm.length);
    assertEquals("4720061d59420a39c21a4c61ee79c8c5f7c93fa66390bfb76c2660d59604c980",
      HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(pcm)));
  }

  @Test
  void bigEndianOutputContainsTheSameSamples() throws Exception {
    byte[] littleEndian = decodeWithLitiengine(readSample(), false);
    byte[] bigEndian = decodeWithLitiengine(readSample(), true);

    assertEquals(littleEndian.length, bigEndian.length);
    for (int i = 0; i < littleEndian.length; i += 2) {
      assertEquals(littleEndian[i], bigEndian[i + 1]);
      assertEquals(littleEndian[i + 1], bigEndian[i]);
    }
  }

  @Test
  void largeCallerBuffersProduceTheSamePcm() throws Exception {
    byte[] mp3 = readSample();

    assertArrayEquals(decodeWithLitiengine(mp3, false), decodeWithLitiengine(mp3, false, 600_000));
  }

  @Test
  void conversionPreservesRateAndChannels() throws Exception {
    try (var source = new Mp3FileReader().getAudioInputStream(new ByteArrayInputStream(readSample()))) {
      var provider = new Mp3FormatConversionProvider();
      var formats = provider.getTargetFormats(AudioFormat.Encoding.PCM_SIGNED, source.getFormat());

      assertEquals(2, formats.length);
      assertEquals(source.getFormat().getSampleRate(), formats[0].getSampleRate());
      assertEquals(source.getFormat().getChannels(), formats[0].getChannels());

      var stereo = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, source.getFormat().getSampleRate(),
        16, 2, 4, source.getFormat().getSampleRate(), false);
      assertThrows(IllegalArgumentException.class, () -> provider.getAudioInputStream(stereo, source));
    }
  }

  private static byte[] decodeWithLitiengine(byte[] mp3Data, boolean bigEndian) throws Exception {
    try (AudioInputStream source = new Mp3FileReader()
      .getAudioInputStream(new ByteArrayInputStream(mp3Data))) {
      var target = targetFormat(source.getFormat(), bigEndian);
      try (var decoded = new Mp3FormatConversionProvider().getAudioInputStream(target, source)) {
        return decoded.readAllBytes();
      }
    }
  }

  private static byte[] decodeWithLitiengine(byte[] mp3Data, boolean bigEndian, int bufferSize) throws Exception {
    try (AudioInputStream source = new Mp3FileReader()
      .getAudioInputStream(new ByteArrayInputStream(mp3Data))) {
      var target = targetFormat(source.getFormat(), bigEndian);
      try (var decoded = new Mp3FormatConversionProvider().getAudioInputStream(target, source);
        var output = new ByteArrayOutputStream()) {
        byte[] buffer = new byte[bufferSize];
        int read;
        while ((read = decoded.read(buffer)) != -1) output.write(buffer, 0, read);
        return output.toByteArray();
      }
    }
  }

  private static AudioFormat targetFormat(AudioFormat source, boolean bigEndian) {
    return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, source.getSampleRate(), 16,
      source.getChannels(), source.getChannels() * 2, source.getSampleRate(), bigEndian);
  }

  private static byte[] readSample() throws IOException {
    try (var stream = Resources.getLocation(MP3_RESOURCE).openStream()) {
      return stream.readAllBytes();
    }
  }
}
