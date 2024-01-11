package de.gurkenlabs.litiengine.sound.spi.mp3;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.nio.ByteBuffer;

import static junit.framework.Assert.*;

class MpegFrameTests {

  @Test
  void testBitReader(){
    var reader = new BitReader((byte)0b11111111, (byte)0b11111011, (byte)0b00111000, (byte)0b11000100);
    var syncRoot = reader.get(11);
    var version = reader.get(2);
    var layer = reader.get(2);
    var protection = reader.get(1);
    var bitRate = reader.get(4);
    var sampleRate = reader.get(2);
    var padding = reader.get(1);
    var priv = reader.get(1);
    var channelMode = reader.get(2);
    var modeExtension = reader.get(2);
    var copyright = reader.get(1);
    var original = reader.get(1);
    var emphasis = reader.get(2);

    assertEquals(0b11111111111, syncRoot);
    assertEquals(0b11, version);
    assertEquals(0b01, layer);
    assertEquals(0b1, protection);
    assertEquals(0b0011, bitRate);
    assertEquals(0b10, sampleRate);
    assertEquals(0b0, padding);
    assertEquals(0b0, priv);
    assertEquals(0b11, channelMode);
    assertEquals(0b00, modeExtension);
    assertEquals(0b0, copyright);
    assertEquals(0b1, original);
    assertEquals(0b00, emphasis);
  }
  @Test
  void testHeaderDecoding() throws UnsupportedAudioFileException {
    var bytes = ByteBuffer.allocate(216).putInt((int)Long.parseLong("11111111111110110011100011000100", 2));
    // bytes.putShort(4, (short)4811); attempt for CRC (not supported yet)
    var frame = new MpegFrame(bytes, 0);

    assertEquals(Mpeg.VERSION_1_0, frame.getVersion());
    assertEquals(Mpeg.LAYER_3, frame.getLayer());
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
