package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.sound.spi.BitReader;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.nio.ByteBuffer;

class MpegFrame {

  /**
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
   * <p>
   * If error_protection flag is set to 1, header is followed by a two byte CRC.
   */
  private static final int FRAME_SYNC = 0b11111111111;

  private static final int CRC_SIZE_IN_BYTES = 2;

  private static final int HEADER_SIZE_IN_BYTES = 4;

  private final int frameOffset;
  private final String version;
  private final String layer;
  private final int bitRate;
  private final int sampleRate;
  private final boolean padding;
  private final boolean isProtected;
  private final boolean privat;
  private final boolean copyright;
  private final boolean original;
  private final String channelMode;
  private final String modeExtension;
  private final String emphasis;

  private byte[] payload;

  private final SideInfo sideInfo;

  public MpegFrame(ByteBuffer byteBuffer, int frameOffset) throws UnsupportedAudioFileException {
    this.frameOffset = frameOffset;
    var bits = new BitReader(byteBuffer, frameOffset);

    var frameSync = bits.get(11);
    if (frameSync != FRAME_SYNC) {
      throw new UnsupportedAudioFileException("Frame sync missing");
    }

    // 1. decode header
    this.version = Mpeg.getVersion(bits.get(2));
    this.layer = Mpeg.getLayer(bits.get(2));
    this.isProtected = bits.getBoolean();
    this.bitRate = Mpeg.getBitRate(bits.get(4), this.version, this.layer);
    this.sampleRate = Mpeg.getSampleRate(bits.get(2), this.version);
    this.padding = bits.getBoolean();
    this.privat = bits.getBoolean();
    this.channelMode = Mpeg.getChannelMode(bits.get(2));
    this.modeExtension = Mpeg.getModeExtension(bits.get(2), this.layer, this.channelMode);
    this.copyright = bits.getBoolean();
    this.original = bits.getBoolean();
    this.emphasis = Mpeg.getEmphasis(bits.get(2));

    if (!this.version.equals(Mpeg.VERSION_1_0) || !this.getLayer().equals(Mpeg.LAYER_3)) {
      throw new UnsupportedAudioFileException("This mpeg decoder only support MPEG version 1.0 layer III audio files.");
    }

    // 2. error check
    if (this.isProtected() && !this.checkCrc(byteBuffer)) {
      throw new UnsupportedAudioFileException("CRC check failed. Inconsistent header data");
    }

    // 3. read payload
    this.readPayload(byteBuffer);

    // 4. get side information from payload
    this.sideInfo = this.decodeSideInfo();
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
    return layer;
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

  public boolean isProtected() {
    return isProtected;
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
    if (layer.equals(Mpeg.LAYER_1)) {
      length = ((48000L * bitRate) / sampleRate) + (pad * 4);
    } else {
      length = ((144000L * bitRate) / sampleRate) + pad;
    }
    return (int) length;
  }

  public SideInfo getSideInfo() {
    return this.sideInfo;
  }


  private boolean checkCrc(ByteBuffer byteBuffer) {
    // read 16 bits (short) after the header
    var crcBytes = new byte[CRC_SIZE_IN_BYTES];
    byteBuffer.get(this.frameOffset + HEADER_SIZE_IN_BYTES, crcBytes);

    // TODO: implement
    return true;
  }

  private void readPayload(ByteBuffer byteBuffer) {
    var payloadOffset = HEADER_SIZE_IN_BYTES + (this.isProtected() ? CRC_SIZE_IN_BYTES : 0);
    var payloadSize = this.getLengthInBytes() - payloadOffset;

    this.payload = new byte[payloadSize];
    byteBuffer.get(this.frameOffset + payloadOffset, this.payload);
  }

  /**
   * Reads the side info from the payload assuming the payload has been read already.
   * <p>
   * Mono   : 136 bits (= 17 bytes)
   * Stereo : 256 bits (= 32 bytes)
   */
  private SideInfo decodeSideInfo() {
    var bits = new BitReader(this.payload);

    // this supports only MPEG 1.0; side info layout is different for MPEG 2 and 2.5
    var si = new SideInfo(bits.get(9), bits.get(this.getChannels() == 1 ? 5 : 3));
    for (var ch = 0; ch < this.getChannels(); ch++) {
      si.channels[ch].scfsi[0] = bits.getBoolean();
      si.channels[ch].scfsi[1] = bits.getBoolean();
      si.channels[ch].scfsi[2] = bits.getBoolean();
      si.channels[ch].scfsi[3] = bits.getBoolean();
    }

    for (var gr = 0; gr < 2; gr++) {
      for (var ch = 0; ch < this.getChannels(); ch++) {
        si.channels[ch].granules[gr].part2_3_length = bits.get(12);
        si.channels[ch].granules[gr].big_values = bits.get(9);
        si.channels[ch].granules[gr].global_gain = bits.get(8);
        si.channels[ch].granules[gr].scalefac_compress = bits.get(4);
        si.channels[ch].granules[gr].window_switching_flag = bits.getBoolean();

        if (si.channels[ch].granules[gr].window_switching_flag) {
          si.channels[ch].granules[gr].block_type = bits.get(2);
          si.channels[ch].granules[gr].mixed_block_flag = bits.getBoolean();

          si.channels[ch].granules[gr].table_select[0] = bits.get(5);
          si.channels[ch].granules[gr].table_select[1] = bits.get(5);

          si.channels[ch].granules[gr].subblock_gain[0] = bits.get(3);
          si.channels[ch].granules[gr].subblock_gain[1] = bits.get(3);
          si.channels[ch].granules[gr].subblock_gain[2] = bits.get(3);

          // Set region_count parameters since they are implicit in this case.

          if (si.channels[ch].granules[gr].block_type == 0) {
            //	 Side info bad: block_type == 0 in split block
            return si;
          } else if (si.channels[ch].granules[gr].block_type == 2 && !si.channels[ch].granules[gr].mixed_block_flag) {
            si.channels[ch].granules[gr].region0_count = 8;
          } else {
            si.channels[ch].granules[gr].region0_count = 7;
          }
          si.channels[ch].granules[gr].region1_count = 20 - si.channels[ch].granules[gr].region0_count;
        } else {
          si.channels[ch].granules[gr].table_select[0] = bits.get(5);
          si.channels[ch].granules[gr].table_select[1] = bits.get(5);
          si.channels[ch].granules[gr].table_select[2] = bits.get(5);
          si.channels[ch].granules[gr].region0_count = bits.get(4);
          si.channels[ch].granules[gr].region1_count = bits.get(3);
          si.channels[ch].granules[gr].block_type = 0;
        }

        si.channels[ch].granules[gr].preflag = bits.getBoolean();
        si.channels[ch].granules[gr].scalefac_scale = bits.getBoolean();
        si.channels[ch].granules[gr].count1table_select = bits.getBoolean();
      }
    }

    return si;
  }

  static class SideInfo {
    final int mainDataBegin;

    final int privateBits;

    final ChannelInfo[] channels;


    SideInfo(int mainDataBegin, int privateBits) {
      this.mainDataBegin = mainDataBegin;
      this.privateBits = privateBits;
      this.channels = new ChannelInfo[]{new ChannelInfo(), new ChannelInfo()};
    }
  }

  static class ChannelInfo {

    /**
     * Scale factor select information.
     * <p/>
     * Layer III contains two granules and the encoder can specify separately for
     * each group of scale factor bands whether the second granule will reuse the
     * scale factor information of the first granule or not. If the value of scfsi is
     * one, then sharing of scale factors is allowed between the granules.
     */
    final boolean[] scfsi;

    final GranuleInfo[] granules;

    ChannelInfo() {
      this.scfsi = new boolean[4];
      this.granules = new GranuleInfo[]{new GranuleInfo(), new GranuleInfo()};
    }
  }

  static class GranuleInfo {
    int part2_3_length = 0;
    int big_values = 0;
    int global_gain = 0;
    int scalefac_compress = 0;
    boolean window_switching_flag = false;
    int block_type = 0;
    boolean mixed_block_flag = false;
    final int[] table_select = new int[3];
    final int[] subblock_gain = new int[3];
    int region0_count = 0;
    int region1_count = 0;
    boolean preflag = false;
    boolean scalefac_scale = false;
    boolean count1table_select = false;
  }
}
