package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.sound.spi.BitReader;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.nio.ByteBuffer;

/**
 * An MPEG frame is organized like this:
 *
 * <pre>
 * +--------+------------------+-----------+----------------+
 * | HEADER | SIDE INFORMATION | MAIN DATA | ANCILLARY DATA |
 * +--------+------------------+-----------+----------------+
 * </pre>
 */
class MpegFrame {

  private static final int HEADER_SIZE_IN_BYTES = 4;

  private static final int CRC_SIZE_IN_BYTES = 2;

  private final Header header;

  private final SideInfo sideInfo;

  private byte[] samples;

  public MpegFrame(ByteBuffer byteBuffer, int frameOffset) throws UnsupportedAudioFileException {

    // 1. decode header
    this.header = new Header(byteBuffer, frameOffset);

    // 2. error check
    if (this.isProtected() && !checkCrc(byteBuffer, frameOffset)) {
      throw new UnsupportedAudioFileException("CRC check failed. Inconsistent header data");
    }

    // 3. get side information from payload
    this.sideInfo = new SideInfo(byteBuffer, frameOffset, this.isProtected(), this.getChannels());

    // 4. decode main data
    this.samples = null;//TODO: this.decodeMainData(byteBuffer, frameOffset);
  }

  public int getBitRate() {
    return this.header.bitRate;
  }

  public String getChannelMode() {
    return this.header.channelMode;
  }

  public boolean isCopyright() {
    return this.header.copyright;
  }

  public String getEmphasis() {
    return this.header.emphasis;
  }

  public String getLayer() {
    return this.header.layer;
  }

  public String getModeExtension() {
    return this.header.modeExtension;
  }

  public boolean isOriginal() {
    return this.header.original;
  }

  public boolean hasPadding() {
    return this.header.padding;
  }

  public boolean isPrivate() {
    return this.header.isPrivate;
  }

  public boolean isProtected() {
    return this.header.isProtected;
  }

  public int getSampleRate() {
    return this.header.sampleRate;
  }

  public int getChannels() {
    return this.getChannelMode().equals(Mpeg.CHANNEL_MODE_MONO) ? 1 : 2;
  }

  public String getVersion() {
    return this.header.version;
  }

  public AudioFormat.Encoding getEncoding() {
    return Mpeg.getEncoding(this.getVersion(), this.header.layer);
  }

  public float getFrameRate() {
    return Mpeg.getFrameRate(this.getVersion(), this.getSampleRate());
  }

  public int getLengthInBytes() {
    return 144000 * this.header.bitRate / this.header.sampleRate + (this.header.padding ? 1 : 0);
  }

  public SideInfo getSideInfo() {
    return this.sideInfo;
  }

  private static boolean checkCrc(ByteBuffer byteBuffer, int frameOffset) {
    // read 16 bits (short) after the header
    var crcBytes = new byte[CRC_SIZE_IN_BYTES];
    byteBuffer.get(frameOffset + HEADER_SIZE_IN_BYTES, crcBytes);

    // TODO: implement
    return true;
  }

  /**
   * The main data does not follow the side information in the bitstream.
   * The main data ends at a location in the bitstream preceding the frame header of the frame at an offset
   * given by the value of main_data_start.
   * <p/>
   * <p>
   * The main data is organized like this:
   * <pre>
   *   +---------------+---------------------------+----------------+
   *   | SCALE FACTORS | HUFFMAN CODED RAW SAMPLES | ANCILLARY INFO |
   *   +---------------+---------------------------+----------------+
   * </pre>
   *
   * @return The decoded samples.
   */
  private byte[] decodeMainData(ByteBuffer byteBuffer, int frameOffset) {

    var mainDataOffset = 0;

    var headerAndSizeInfoSize = HEADER_SIZE_IN_BYTES + (this.isProtected() ? CRC_SIZE_IN_BYTES : 0) +
      Mpeg.getSideInfoLength(this.getChannels());

    // If the value is 0, then the main data follows immediately the side information
    if (this.getSideInfo().mainDataBegin == 0) {
      mainDataOffset = headerAndSizeInfoSize;
    } else {
      mainDataOffset = -this.getSideInfo().mainDataBegin;
    }

    var mainDataSize = this.getLengthInBytes() - headerAndSizeInfoSize;

    var mainData = new byte[mainDataSize];
    byteBuffer.get(frameOffset + mainDataOffset, mainData);

    var bits = new BitReader(mainData);

    final ScaleFactors[][] scaleFactors = {
      {new ScaleFactors(), new ScaleFactors()},
      {new ScaleFactors(), new ScaleFactors()}
    };

    for (var gr = 0; gr < 2; gr++) {
      for (var ch = 0; ch < this.getChannels(); ch++) {
        // 1. decode scale factors
        // From the bitstream only the scale factor indices are found but not the scale factors

        decodeScaleFactors(bits, scaleFactors, gr, ch);

        // 2. decode huffman data
        decodeHuffmanBits(bits, gr, ch);

        // 3. dequantize sample
      }
    }
    return new byte[0]; // TODO
  }

  private void decodeHuffmanBits(BitReader bits, int gr, int ch) {

  }

  private void decodeScaleFactors(BitReader bits, ScaleFactors[][] scaleFactors, int gr, int ch) {
    final int SHORT_SWITCH_POINT = 6;
    final int LONG_SWITCH_POINT = 12;
    final var granule = this.getSideInfo().channels[ch].granules[gr];
    final var slen1 = granule.slen1(); // slen1 is for bands 3-5
    final var slen2 = granule.slen2(); // slen2 is for bands 6-11

    if (granule.window_switching_flag && granule.block_type == SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS) {
      if (granule.mixed_block_flag) {
        for (var sfb = 0; sfb < 8; sfb++) {
          scaleFactors[ch][gr].l[sfb] = bits.get(slen1);
        }
      }

      for (var sfb = 0; sfb < 12; sfb++) {
        // for MIXED, there are no short values for the first 3 scale factor bands
        if (granule.mixed_block_flag && sfb < 3) {
          continue;
        }

        final var bitsToRead = sfb < SHORT_SWITCH_POINT ? slen1 : slen2;
        for (var window = 0; window < 3; window++) {
          scaleFactors[ch][gr].s[window][sfb] = bits.get(bitsToRead);
        }
      }
    } else {  // LONG types 0,1,3
      for (var sfb = 0; sfb < 20; sfb++) {
        final var bitsToRead = sfb < LONG_SWITCH_POINT ? slen1 : slen2;
        var scaleFactor = reuseScaleFactor(gr, ch, sfb) ? scaleFactors[ch][0].l[sfb] : bits.get(bitsToRead);
        scaleFactors[ch][gr].l[sfb] = scaleFactor;
      }
    }
  }

  boolean reuseScaleFactor(int granule, int channel, int scaleFactorBand) {
    if (granule == 0) {
      return false;
    }

    // Scale factor bands 0-5
    if (scaleFactorBand < 6) {
      return this.getSideInfo().channels[channel].scfsi[0];
    }

    // Scale factor bands 6-10
    if (scaleFactorBand < 11) {
      return this.getSideInfo().channels[channel].scfsi[1];
    }

    // Scale factor bands 11-15
    if (scaleFactorBand < 16) {
      return this.getSideInfo().channels[channel].scfsi[2];
    }

    // Scale factor bands 16-20
    if (scaleFactorBand < 21) {
      return this.getSideInfo().channels[channel].scfsi[3];
    }

    return false;
  }

  /**
   * The frame header is organized like this:
   *
   * <pre>
   * |     1st byte   |     2nd byte         |      3rd byte         |       4th byte          |
   * +----------------+----------------------+-----------------------+-------------------------+
   * | 1 1 1 1 1 1 1 1 1 1 1 | 0 0 | 0 0 | 0 | 0 0 0 0 | 0 0 | 0 | 0 | 0 0 | 0 0 | 0 | 0 | 0 0 |
   * +-----------------------+-----+-----+---+---------+-----+---+---+-----+-----+---+---+-----+
   *  \__________ __________/ \_ _/ \_ _/ \ / \___ ___/ \_ _/ \ / \ / \_ _/ \_ _/ \ / \ / \_ _/
   *             V              V     V    V      V       V    V   V    V     V    V   V    V
   *         syncword          ID   layer  | bitrate_index|    | private|     |    | org/cop|
   *                                error_protection      | padding   mode    | copyright emphasis
   *                                             sampling_frequency     mode_extension
   * </pre>
   * <p>
   * If error_protection flag is set to 1, header is followed by a two byte CRC.
   */
  static class Header {
    private static final int FRAME_SYNC = 0b11111111111;

    final String version;
    final String layer;
    final int bitRate;
    final int sampleRate;
    final boolean padding;
    final boolean isProtected;
    final boolean isPrivate;
    final boolean copyright;
    final boolean original;
    final String channelMode;
    final String modeExtension;
    final String emphasis;

    Header(ByteBuffer byteBuffer, int frameOffset) throws UnsupportedAudioFileException {
      var bits = new BitReader(byteBuffer, frameOffset);
      var frameSync = bits.get(11);
      if (frameSync != FRAME_SYNC) {
        throw new UnsupportedAudioFileException("Frame sync missing");
      }

      this.version = Mpeg.getVersion(bits.get(2));
      this.layer = Mpeg.getLayer(bits.get(2));
      if (!this.version.equals(Mpeg.VERSION_1_0) || !this.layer.equals(Mpeg.LAYER_3)) {
        throw new UnsupportedAudioFileException("This mpeg decoder only support MPEG version 1.0 layer III (MP3) audio files.");
      }

      this.isProtected = bits.getBoolean();
      this.bitRate = Mpeg.getBitRate(bits.get(4));
      this.sampleRate = Mpeg.getSampleRate(bits.get(2));
      this.padding = bits.getBoolean();
      this.isPrivate = bits.getBoolean();
      this.channelMode = Mpeg.getChannelMode(bits.get(2));
      this.modeExtension = Mpeg.getModeExtension(bits.get(2), this.channelMode);
      this.copyright = bits.getBoolean();
      this.original = bits.getBoolean();
      this.emphasis = Mpeg.getEmphasis(bits.get(2));
    }
  }

  static class SideInfo {
    /**
     * A pointer that points to the beginning of the main data. The variable has
     * nine bits and specifies the location of the main data as a negative offset
     * (jumping backwards) in bytes from the first byte of the audio sync word.
     * The number of bytes of the header and side information are not taken into
     * account while calculating the location of the main data. This is called bit
     * reservoir technique and it allows the encoder to use some extra bits while
     * encoding a difficult frame. Since it is nine bits long, it can point upto
     * 29 −1 = 511 bytes in front of the header. If the value of main_data_begin is
     * zero, then the main data follows immediately the side information.
     */
    final int mainDataBegin;

    final int privateBits;

    final Channel[] channels;

    /**
     * Reads the side info from the bytebuffer.
     * The side information is organized like this:
     * <pre>
     *   +-----------------+--------------+-------+-------------------------+-------------------------+
     *   | MAIN_DATA_BEGIN | PRIVATE_BITS | SCFSI | SIDE_INFO_FOR_GRANULE_1 | SIDE_INFO_FOR_GRANULE_2 |
     *   +-----------------+--------------+-------+-------------------------+-------------------------+
     * </pre>
     *
     * @return The decoded side information from the provided payload.
     */
    SideInfo(ByteBuffer byteBuffer, int frameOffset, boolean isProtected, int channels) {
      var payloadOffset = HEADER_SIZE_IN_BYTES + (isProtected ? CRC_SIZE_IN_BYTES : 0);

      var payload = new byte[Mpeg.getSideInfoLength(channels)];
      byteBuffer.get(frameOffset + payloadOffset, payload);

      var bits = new BitReader(payload);

      this.mainDataBegin = bits.get(9);
      this.privateBits = bits.get(channels == 1 ? 5 : 3);
      this.channels = new Channel[]{new Channel(), new Channel()};

      // this supports only MPEG 1.0; side info layout is different for MPEG 2 and 2.5
      for (var ch = 0; ch < channels; ch++) {
        this.channels[ch].scfsi[0] = bits.getBoolean();
        this.channels[ch].scfsi[1] = bits.getBoolean();
        this.channels[ch].scfsi[2] = bits.getBoolean();
        this.channels[ch].scfsi[3] = bits.getBoolean();
      }

      // Layer 3 frames are split into two "granules" of 576 samples (due to backward compatibility with layer 2)
      // this has been simplified with MPEG-2 encoder which only has 1 granule per frame
      for (var gr = 0; gr < 2; gr++) {
        for (var ch = 0; ch < channels; ch++) {
          this.channels[ch].granules[gr].part2_3_length = bits.get(12);
          this.channels[ch].granules[gr].big_values = bits.get(9);
          this.channels[ch].granules[gr].global_gain = bits.get(8);
          this.channels[ch].granules[gr].scalefac_compress = bits.get(4);
          this.channels[ch].granules[gr].window_switching_flag = bits.getBoolean();

          if (this.channels[ch].granules[gr].window_switching_flag) {
            this.channels[ch].granules[gr].block_type = bits.get(2);
            this.channels[ch].granules[gr].mixed_block_flag = bits.getBoolean();

            this.channels[ch].granules[gr].table_select[0] = bits.get(5);
            this.channels[ch].granules[gr].table_select[1] = bits.get(5);

            this.channels[ch].granules[gr].subblock_gain[0] = bits.get(3);
            this.channels[ch].granules[gr].subblock_gain[1] = bits.get(3);
            this.channels[ch].granules[gr].subblock_gain[2] = bits.get(3);

            // Set region_count parameters since they are implicit in this case.

            if (this.channels[ch].granules[gr].block_type == 0) {
              //	 Side info bad: block_type == 0 in split block
              return;
            } else if (this.channels[ch].granules[gr].block_type == 2 && !this.channels[ch].granules[gr].mixed_block_flag) {
              this.channels[ch].granules[gr].region0_count = 8;
            } else {
              this.channels[ch].granules[gr].region0_count = 7;
            }
            this.channels[ch].granules[gr].region1_count = 20 - this.channels[ch].granules[gr].region0_count;
          } else {
            this.channels[ch].granules[gr].table_select[0] = bits.get(5);
            this.channels[ch].granules[gr].table_select[1] = bits.get(5);
            this.channels[ch].granules[gr].table_select[2] = bits.get(5);
            this.channels[ch].granules[gr].region0_count = bits.get(4);
            this.channels[ch].granules[gr].region1_count = bits.get(3);
            this.channels[ch].granules[gr].block_type = 0;
          }

          this.channels[ch].granules[gr].preflag = bits.getBoolean();
          this.channels[ch].granules[gr].scalefac_scale = bits.getBoolean();
          this.channels[ch].granules[gr].count1table_select = bits.getBoolean();
        }
      }
    }

    static class Channel {

      /**
       * Scale factor select information.
       * <p/>
       * Layer III contains two granules and the encoder can specify separately for
       * each group of scale factor bands whether the second granule will reuse the
       * scale factor information of the first granule or not. If the value of scfsi is
       * true, then sharing of scale factors is allowed between the granules.
       * <p/>
       * Irrelevant for MPEG-2 which only has one granule per frame.
       */
      final boolean[] scfsi;

      final Granule[] granules;

      Channel() {
        this.scfsi = new boolean[4];
        this.granules = new Granule[]{new Granule(), new Granule()};
      }
    }

    static class Granule {
      static final int BLOCK_TYPE_RESERVED = 0;
      static final int BLOCK_TYPE_START_BLOCK = 1;
      static final int BLOCK_TYPE_3_SHORT_WINDOWS = 2;
      static final int BLOCK_TYPE_END = 3;

      static final int[][] slen =
        {
          {0, 0, 0, 0, 3, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4},
          {0, 1, 2, 3, 0, 1, 2, 3, 1, 2, 3, 1, 2, 3, 2, 3}
        };

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

      int slen1() {
        return slen[0][scalefac_compress];
      }

      int slen2() {
        return slen[1][scalefac_compress];
      }
    }
  }


  static class ScaleFactors {
    final int[] l = new int[23];         /* [cb] */
    final int[][] s = new int[3][13];         /* [window][cb] */
  }
}
