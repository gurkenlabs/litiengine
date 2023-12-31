package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.util.io.Codec;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

class MpegFrame {
  private static final int FRAME_SYNC = 0x7FF;
  private static final long BITMASK_FRAME_SYNC = 0xFFE00000L;
  private static final long BITMASK_VERSION = 0x180000L;
  private static final long BITMASK_LAYER = 0x60000L;
  private static final long BITMASK_PROTECTION = 0x10000L;
  private static final long BITMASK_BITRATE = 0xF000L;
  private static final long BITMASK_SAMPLE_RATE = 0xC00L;
  private static final long BITMASK_PADDING = 0x200L;
  private static final long BITMASK_PRIVATE = 0x100L;
  private static final long BITMASK_CHANNEL_MODE = 0xC0L;
  private static final long BITMASK_MODE_EXTENSION = 0x30L;
  private static final long BITMASK_COPYRIGHT = 0x8L;
  private static final long BITMASK_ORIGINAL = 0x4L;
  private static final long BITMASK_EMPHASIS = 0x3L;

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
  public MpegFrame(byte frameData1, byte frameData2, byte frameData3, byte frameData4) throws UnsupportedAudioFileException {
    long frameHeader = Codec.decodeInteger(frameData1, frameData2, frameData3, frameData4);

    var frameSync = extractField(frameHeader, BITMASK_FRAME_SYNC);
    if (frameSync != FRAME_SYNC) {
      throw new UnsupportedAudioFileException("Frame sync missing");
    }

    this.version = Mpeg.getVersion(extractField(frameHeader, BITMASK_VERSION));
    this.layer = Mpeg.getLayer(extractField(frameHeader, BITMASK_LAYER));
    this.bitRate = Mpeg.getBitRate(extractField(frameHeader, BITMASK_BITRATE), this.version, this.layer);
    this.sampleRate = Mpeg.getSampleRate(extractField(frameHeader, BITMASK_SAMPLE_RATE), this.version);
    this.channelMode = Mpeg.getChannelMode(extractField(frameHeader, BITMASK_CHANNEL_MODE));
    this.modeExtension = Mpeg.getModeExtension(extractField(frameHeader, BITMASK_MODE_EXTENSION), this.layer, this.channelMode);
    this.emphasis = Mpeg.getEmphasis(extractField(frameHeader, BITMASK_EMPHASIS));
    this.protection = extractField(frameHeader, BITMASK_PROTECTION) == 1;
    this.padding = extractField(frameHeader, BITMASK_PADDING) == 1;
    this.privat = extractField(frameHeader, BITMASK_PRIVATE) == 1;
    this.copyright = extractField(frameHeader, BITMASK_COPYRIGHT) == 1;
    this.original = extractField(frameHeader, BITMASK_ORIGINAL) == 1;
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


  private static int extractField(long frameHeader, long bitMask) {
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
