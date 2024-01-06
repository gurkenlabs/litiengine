package de.gurkenlabs.litiengine.sound.spi.mp3;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.nio.ByteBuffer;

import static junit.framework.Assert.*;

class MpegFrameTests {
  @Test
  void testHeaderDecoding() throws UnsupportedAudioFileException {
    var bytes = ByteBuffer.allocate(6).putInt((int)Long.parseLong("11111111111110110011100011000100", 2));
    bytes.putShort(4, (short)4811);
    var frame = new MpegFrame(bytes, 0);

    assertEquals("1.0", frame.getVersion());
    assertEquals(Mpeg.LAYERS[3], frame.getLayer());
    assertEquals(32000, frame.getSampleRate());
    assertEquals(48, frame.getBitRate());
    assertEquals(Mpeg.CHANNEL_MODE_MONO, frame.getChannelMode());
    assertEquals(Mpeg.MODE_EXTENSION_NA, frame.getModeExtension());
    assertEquals(Mpeg.EMPHASIS_NONE, frame.getEmphasis());
    assertFalse(frame.hasPadding());
    assertTrue(frame.isProtected());
    assertFalse(frame.isPrivate());
    assertFalse(frame.isCopyright());
    assertTrue(frame.isOriginal());
  }
}
