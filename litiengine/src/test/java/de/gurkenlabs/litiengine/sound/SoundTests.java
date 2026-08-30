package de.gurkenlabs.litiengine.sound;

import de.gurkenlabs.litiengine.resources.Resources;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SoundTests {
  @Test
  void loadsMp3AsPcm() throws Exception {
    try (var stream = Resources.getLocation("de/gurkenlabs/litiengine/resources/sample.mp3").openStream()) {
      var sound = new Sound(stream, "sample.mp3");

      assertEquals(AudioFormat.Encoding.PCM_SIGNED, sound.getFormat().getEncoding());
      assertEquals(32000.0f, sound.getFormat().getSampleRate());
      assertEquals(1, sound.getFormat().getChannels());
      assertEquals(963072, sound.getStreamData().length);
    }
  }

  @Test
  void loadsExtensiblePcmWavAsPcm() throws Exception {
    byte[] wav = extensiblePcmWav();

    var sound = new Sound(new ByteArrayInputStream(wav), "issue-780.wav");

    assertEquals(wav.length, sound.getRawData().length);
    assertEquals(AudioFormat.Encoding.PCM_SIGNED, sound.getFormat().getEncoding());
    assertEquals(44100.0f, sound.getFormat().getSampleRate());
    assertEquals(16, sound.getFormat().getSampleSizeInBits());
    assertEquals(2, sound.getFormat().getChannels());
    assertEquals(8, sound.getStreamData().length);
  }

  private static byte[] extensiblePcmWav() {
    byte[] pcm = new byte[12];
    var wav = ByteBuffer.allocate(68 + pcm.length).order(ByteOrder.LITTLE_ENDIAN);
    wav.put("RIFF".getBytes(StandardCharsets.US_ASCII));
    wav.putInt(wav.capacity() - 8);
    wav.put("WAVEfmt ".getBytes(StandardCharsets.US_ASCII));
    wav.putInt(40);
    wav.putShort((short) 0xfffe);
    wav.putShort((short) 2);
    wav.putInt(44100);
    wav.putInt(44100 * 6);
    wav.putShort((short) 6);
    wav.putShort((short) 24);
    wav.putShort((short) 22);
    wav.putShort((short) 24);
    wav.putInt(3);
    wav.put(new byte[]{
      1, 0, 0, 0, 0, 0, 0x10, 0, (byte) 0x80, 0, 0, (byte) 0xaa, 0, 0x38, (byte) 0x9b, 0x71
    });
    wav.put("data".getBytes(StandardCharsets.US_ASCII));
    wav.putInt(pcm.length);
    wav.put(pcm);
    return wav.array();
  }
}
