package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.util.io.Codec;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

class MpegFrame {

  /**
   *
   * The frame header scheme is as follows (compatible with MPEG 2.5):
   *
   * <pre>
   * |     1st byte   |     2nd byte         |      3rd byte         |       4th byte          |
   * | 1 1 1 1 1 1 1 1 1 1 1 | 0 0 | 0 0 | 0 | 0 0 0 0 | 0 0 | 0 | 0 | 0 0 | 0 0 | 0 | 0 | 0 0 |
   *  \__________ __________/ \_ _/ \_ _/ \ / \___ ___/ \_ _/ \ / \ / \_ _/ \_ _/ \ / \ / \_ _/
   *             V              V     V    V      V       V    V   V    V     V    V   V    V
   *         syncword          ID   layer  | bitrate_index|    | private|     |    | org/cop|
   *                                error_protection      | padding   mode    | copyright emphasis
   *                                             sampling_frequency     mode_extension
   * </pre>
   *
   * If error_protection flag is set to 1, header is followed by a two byte CRC.
   *
   */
  private static final long[] BITMASKS = new long[]{
    Long.parseLong("11111111111", 2), // FRAME_SYNC
    Long.parseLong("11111111111000000000000000000000", 2), // BITMASK_FRAME_SYNC
    Long.parseLong("00000000000110000000000000000000", 2), // BITMASK_VERSION
    Long.parseLong("00000000000001100000000000000000", 2), // BITMASK_LAYER
    Long.parseLong("00000000000000010000000000000000", 2), // BITMASK_PROTECTION
    Long.parseLong("00000000000000001111000000000000", 2), // BITMASK_BITRATE
    Long.parseLong("00000000000000000000110000000000", 2), // BITMASK_SAMPLE_RATE
    Long.parseLong("00000000000000000000001000000000", 2), // BITMASK_PADDING
    Long.parseLong("00000000000000000000000100000000", 2), // BITMASK_PRIVATE
    Long.parseLong("00000000000000000000000011000000", 2), // BITMASK_CHANNEL_MODE
    Long.parseLong("00000000000000000000000000110000", 2), // BITMASK_MODE_EXTENSION
    Long.parseLong("00000000000000000000000000001000", 2), // BITMASK_COPYRIGHT
    Long.parseLong("00000000000000000000000000000100", 2), // BITMASK_ORIGINAL
    Long.parseLong("00000000000000000000000000000011", 2), // BITMASK_EMPHASIS
  };

  private final String version;
  private final int layer;
  private final int bitRate;
  private final int sampleRate;
  private final boolean padding;
  private final boolean protection;
  private final boolean privat;
  private final boolean copyright;
  private final boolean original;
  private final String channelMode;
  private final String modeExtension;
  private final String emphasis;

  public MpegFrame(byte frameData1, byte frameData2, byte frameData3, byte frameData4) throws UnsupportedAudioFileException {
    int frameHeader = Codec.decodeInteger(frameData1, frameData2, frameData3, frameData4);

    var frameSync = extractField(frameHeader, BITMASKS[1]);
    if (frameSync != BITMASKS[0]) {
      throw new UnsupportedAudioFileException("Frame sync missing");
    }

    this.version = Mpeg.getVersion(extractField(frameHeader, BITMASKS[2]));
    this.layer = Mpeg.getLayer(extractField(frameHeader, BITMASKS[3]));
    this.protection = extractField(frameHeader, BITMASKS[4]) == 1;
    this.bitRate = Mpeg.getBitRate(extractField(frameHeader, BITMASKS[5]), this.version, this.layer);
    this.sampleRate = Mpeg.getSampleRate(extractField(frameHeader, BITMASKS[6]), this.version);
    this.padding = extractField(frameHeader, BITMASKS[7]) == 1;
    this.privat = extractField(frameHeader, BITMASKS[8]) == 1;
    this.channelMode = Mpeg.getChannelMode(extractField(frameHeader, BITMASKS[9]));
    this.modeExtension = Mpeg.getModeExtension(extractField(frameHeader, BITMASKS[10]), this.layer, this.channelMode);
    this.copyright = extractField(frameHeader, BITMASKS[11]) == 1;
    this.original = extractField(frameHeader, BITMASKS[12]) == 1;
    this.emphasis = Mpeg.getEmphasis(extractField(frameHeader, BITMASKS[13]));
  }

  public int getBitRate() {
    return bitRate;
  }

  public String getChannelMode() {
    return channelMode;
  }

  public boolean isCopyright() {
    return copyright;
  }

  public String getEmphasis() {
    return emphasis;
  }

  public String getLayer() {
    return Mpeg.LAYERS[layer];
  }

  public String getModeExtension() {
    return modeExtension;
  }

  public boolean isOriginal() {
    return original;
  }

  public boolean hasPadding() {
    return padding;
  }

  public boolean isPrivate() {
    return privat;
  }

  public boolean isProtection() {
    return protection;
  }

  public int getSampleRate() {
    return sampleRate;
  }

  public int getChannels() {
    return this.getChannelMode().equals(Mpeg.CHANNEL_MODE_MONO) ? 1 : 2;
  }

  public String getVersion() {
    return version;
  }

  public AudioFormat.Encoding getEncoding() {
    return Mpeg.getEncoding(this.getVersion(), this.layer);
  }

  public float getFrameRate() {
    return Mpeg.getFrameRate(this.getVersion(), this.layer, this.getSampleRate());
  }

  public int getLengthInBytes() {
    long length;
    int pad = padding ? 1 : 0;
    if (layer == 1) {
      length = ((48000L * bitRate) / sampleRate) + (pad * 4);
    } else {
      length = ((144000L * bitRate) / sampleRate) + pad;
    }
    return (int) length;
  }


  private static int extractField(int frameHeader, long bitMask) {
    int shiftBy = 0;
    for (int i = 0; i <= 31; i++) {
      if (((bitMask >> i) & 1) != 0) {
        shiftBy = i;
        break;
      }
    }
    return (int) ((frameHeader >> shiftBy) & (bitMask >> shiftBy));
  }
}
