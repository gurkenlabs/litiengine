package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.sound.spi.BitReader;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.nio.ByteBuffer;

import static junit.framework.Assert.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class MpegFrameTests {

  private static final byte[] EXAMPLE_HEADER = new byte[]{(byte)0b11111111, (byte)0b11111011, (byte)0b00111000, (byte)0b11000100};

  private static final byte[] EXAMPLE_SIDE_INFO = new byte[]{-52, -123, 71, 78, 13, 36, 81, 1, -127, 36, -87, -127, -84, 12, 112, -92, -57};

  @Test
  void testBitReader(){
    var reader = new BitReader(EXAMPLE_HEADER);
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
    var frame = new MpegFrame(exampleMpegData(), 0);

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

  @Test
  void testSideInfoDecoding() throws UnsupportedAudioFileException{
    var frame = new MpegFrame(exampleMpegData(), 0);

    assertEquals(409, frame.getSideInfo().mainDataBegin);
    assertEquals(1, frame.getSideInfo().privateBits);
    assertArrayEquals(new boolean[]{false, true, false, true}, frame.getSideInfo().channels[0].scfsi);
    assertEquals(467, frame.getSideInfo().channels[0].granules[0].part2_3_length);
    assertEquals(262, frame.getSideInfo().channels[0].granules[0].big_values);
    assertEquals(146, frame.getSideInfo().channels[0].granules[0].global_gain);
    assertEquals(2, frame.getSideInfo().channels[0].granules[0].scalefac_compress);
    assertEquals(0, frame.getSideInfo().channels[0].granules[0].block_type);
    assertEquals(0, frame.getSideInfo().channels[0].granules[0].region0_count);
    assertEquals(0, frame.getSideInfo().channels[0].granules[0].region1_count);
    assertArrayEquals(new int[]{16, 3, 0}, frame.getSideInfo().channels[0].granules[0].table_select);
    assertArrayEquals(new int[]{0, 0, 4}, frame.getSideInfo().channels[0].granules[0].subblock_gain);
    assertTrue(frame.getSideInfo().channels[0].granules[0].window_switching_flag);
    assertFalse(frame.getSideInfo().channels[0].granules[0].mixed_block_flag);
    assertFalse(frame.getSideInfo().channels[0].granules[0].preflag);
    assertFalse(frame.getSideInfo().channels[0].granules[0].scalefac_scale);
    assertFalse(frame.getSideInfo().channels[0].granules[0].count1table_select);
  }

  ByteBuffer exampleMpegData(){
    var bytes = ByteBuffer.allocate(216);
    bytes.put(EXAMPLE_HEADER);
    bytes.put((byte)0); // TODO: write CRC (not supported yet)
    bytes.put((byte)0);
    bytes.put(EXAMPLE_SIDE_INFO);
    return bytes;
  }
}
