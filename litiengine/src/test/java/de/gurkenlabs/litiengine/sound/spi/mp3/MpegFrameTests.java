package de.gurkenlabs.litiengine.sound.spi.mp3;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.nio.ByteBuffer;

import static junit.framework.Assert.*;

class MpegFrameTests {
  @Test
  void testHeaderDecoding() throws UnsupportedAudioFileException {
    var bytes = ByteBuffer.allocate(4).putInt((int)Long.parseLong("11111111111110110011100011000100", 2)).array();
    var frame = new MpegFrame(bytes[0], bytes[1], bytes[2], bytes[3]);

    assertEquals("1.0", frame.getVersion());
    assertEquals(Mpeg.LAYERS[3], frame.getLayer());
    assertEquals(32000, frame.getSampleRate());
    assertEquals(48, frame.getBitRate());
    assertEquals(Mpeg.CHANNEL_MODE_MONO, frame.getChannelMode());
    assertEquals(Mpeg.MODE_EXTENSION_NA, frame.getModeExtension());
    assertEquals(Mpeg.EMPHASIS_NONE, frame.getEmphasis());
    assertFalse(frame.hasPadding());
    assertTrue(frame.isProtection());
    assertFalse(frame.isPrivate());
    assertFalse(frame.isCopyright());
    assertTrue(frame.isOriginal());
  }
}
