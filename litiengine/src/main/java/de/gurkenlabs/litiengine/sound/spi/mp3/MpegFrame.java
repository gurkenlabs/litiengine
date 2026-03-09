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

  private final MainData mainData;

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
    this.mainData = new MainData(byteBuffer, frameOffset, this);
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

  public float[][][] getSamples() {
    return this.mainData.getSamples();
  }

  private static boolean checkCrc(ByteBuffer byteBuffer, int frameOffset) {
    // read 16 bits (short) after the header
    var crcBytes = new byte[CRC_SIZE_IN_BYTES];
    byteBuffer.get(frameOffset + HEADER_SIZE_IN_BYTES, crcBytes);

    // TODO: implement
    return true;
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
      var bits = new BitReader(byteBuffer, frameOffset, 0);
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

  /**
   * The side information is organized like this:
   * <pre>
   *   +-----------------+--------------+-------+-------------------------+-------------------------+
   *   | MAIN_DATA_BEGIN | PRIVATE_BITS | SCFSI | SIDE_INFO_FOR_GRANULE_1 | SIDE_INFO_FOR_GRANULE_2 |
   *   +-----------------+--------------+-------+-------------------------+-------------------------+
   * </pre>
   */
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
     * @param byteBuffer The buffer to read the info from.
     * @param frameOffset The offset of the frame within the buffer.
     * @param isProtected A flag indicating whether the MPEG frame is protected.
     * @param channels The number of channels of the MPEG frame.
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

    boolean reuseScaleFactor(int granule, int channel, int scaleFactorBand) {
      if (granule == 0) {
        return false;
      }

      // Scale factor bands 0-5
      if (scaleFactorBand < 6) {
        return this.channels[channel].scfsi[0];
      }

      // Scale factor bands 6-10
      if (scaleFactorBand < 11) {
        return this.channels[channel].scfsi[1];
      }

      // Scale factor bands 11-15
      if (scaleFactorBand < 16) {
        return this.channels[channel].scfsi[2];
      }

      // Scale factor bands 16-20
      if (scaleFactorBand < 21) {
        return this.channels[channel].scfsi[3];
      }

      return false;
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
  static class MainData {
    private final float[][][] samples; // [channel][granule][576]
    private final MpegFrame frame;
    private final ScaleFactors[][] scaleFactors; // [channel][granule] - needed for dequantization

    // Scale factor band boundaries for different block types (index 0-6 for different block types)
    // These are for 44.1kHz - other sample rates scale differently
    private static final int[] SCALE_FACTOR_BANDS_LONG = {0, 4, 8, 12, 16, 20, 24, 30, 36, 44, 52, 60, 70, 80, 90, 102, 116, 132, 150, 172, 198, 228, 260, 296, 338, 384, 436, 496, 566, 576};
    private static final int[] SCALE_FACTOR_BANDS_SHORT = {0, 4, 8, 12, 16, 22, 30, 40, 52, 66, 84, 106, 136, 192, 576};

    // Preflag multipliers for high-frequency bands (index is scalefactor band)
    // These are used when preflag is enabled to amplify high-frequency bands
    private static final int[] PREFACTORS = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3,
        4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6,
        7, 7, 7, 7, 8, 8, 8, 8
    };

    // Number of lines per scale factor band for long blocks (44.1kHz)
    private static final int[] SCALE_FACTOR_BAND_WIDTHS_LONG = {
        4, 4, 4, 4, 4, 4, 6, 6, 8, 8, 10, 10, 12, 14, 16, 18, 20, 24, 26, 30, 32, 36, 38, 42, 48, 52, 60, 70, 10
    };

  MainData(ByteBuffer byteBuffer, int frameOffset, MpegFrame frame) {
    this.frame = frame;
    this.samples = new float[frame.getChannels()][2][576];
    this.scaleFactors = new ScaleFactors[frame.getChannels()][2];

    // Calculate the header and side info size
    var headerAndSideInfoSize = HEADER_SIZE_IN_BYTES + (frame.isProtected() ? CRC_SIZE_IN_BYTES : 0) +
      Mpeg.getSideInfoLength(frame.getChannels());

    // The mainDataBegin field specifies the offset from the start of the frame to the main data
    // If it's 0, main data starts right after side info
    // If it's > 0, main data may be in a previous frame (bit reservoir)
    var mainDataBegin = frame.getSideInfo().mainDataBegin;
    
    // Determine the actual offset where main data starts
    var mainDataOffset = 0;
    if (mainDataBegin == 0) {
      mainDataOffset = headerAndSideInfoSize;
    } else {
      // For bit reservoir, we need data from the previous frame
      // This is complex to handle correctly - for now, skip main data decoding if offset would be negative
      mainDataOffset = headerAndSideInfoSize - mainDataBegin;
    }
    
    // Check if we have enough data to read main data
    var frameLength = frame.getLengthInBytes();
    if (mainDataOffset < 0 || mainDataOffset >= frameLength) {
      // Cannot read main data - insufficient buffer data
      // Initialize with zeros
      return;
    }
    
    var availableMainData = frameLength - mainDataOffset;
    if (availableMainData <= 0) {
      return;
    }

    // TODO: if sync header is found skip header + side info bits and then continue reading main data
    // this is because the main data can span a bit stream area that overlaps the frame header/side info (see Appendix A.7 - Layer III bitstream organization)
    var mainDataSize = frameLength - headerAndSideInfoSize;

    var mainData = new byte[mainDataSize];
    byteBuffer.get(frameOffset + mainDataOffset, mainData);

    var bits = new BitReader(mainData);

    // Initialize scale factors for each channel and granule
    for (var ch = 0; ch < frame.getChannels(); ch++) {
      for (var gr = 0; gr < 2; gr++) {
        this.scaleFactors[ch][gr] = new ScaleFactors();
      }
    }

    for (var gr = 0; gr < 2; gr++) {
      for (var ch = 0; ch < frame.getChannels(); ch++) {
        // 1. decode scale factors
        // From the bitstream only the scale factor indices are found but not the scale factors
        decodeScaleFactors(bits, this.scaleFactors, gr, ch, frame.getSideInfo());

        // 2. decode huffman data
        decodeHuffmanBits(bits, gr, ch);

        // 3. dequantize sample
        dequantize(gr, ch);
      }
    }
  }

    private void decodeHuffmanBits(BitReader bits, int gr, int ch) {
      var sideInfo = this.frame.getSideInfo();
      var granule = sideInfo.channels[ch].granules[gr];

      int[] xr = new int[576];

      // Calculate region boundaries based on scale factor bands
      int region0Start = 0;
      int region1Start = getRegionStart(granule, 0);
      int region2Start = getRegionStart(granule, 1);
      int bigValuesEnd = granule.big_values * 2;

      // Ensure we don't exceed array bounds
      bigValuesEnd = Math.min(bigValuesEnd, 576);

      // Decode big_values region with up to 3 different Huffman tables
      // Region 0
      if (granule.table_select[0] != 0 && region0Start < region1Start) {
        decodeBigValuesRegion(bits, xr, region0Start, Math.min(region1Start, bigValuesEnd),
            HuffmanCode.getTable(granule.table_select[0]));
      }

      // Region 1
      if (granule.table_select[1] != 0 && region1Start < region2Start && region1Start < bigValuesEnd) {
        decodeBigValuesRegion(bits, xr, region1Start, Math.min(region2Start, bigValuesEnd),
            HuffmanCode.getTable(granule.table_select[1]));
      }

      // Region 2
      if (granule.table_select[2] != 0 && region2Start < bigValuesEnd) {
        decodeBigValuesRegion(bits, xr, region2Start, bigValuesEnd,
            HuffmanCode.getTable(granule.table_select[2]));
      }

      // Decode count1 region (quadruples)
      decodeCount1Region(bits, xr, bigValuesEnd);

      // Store in samples array (will be dequantized later)
      for (int i = 0; i < 576; i++) {
        this.samples[ch][gr][i] = xr[i];
      }
    }

    private int getRegionStart(SideInfo.Granule granule, int region) {
      int bandIndex;
      if (region == 0) {
        bandIndex = granule.region0_count;
      } else {
        bandIndex = granule.region0_count + granule.region1_count + 2;
      }

      // Convert scale factor band index to frequency line
      // Using simplified lookup based on block type
      int[] bands = getScaleFactorBands(granule.block_type);
      if (bandIndex >= bands.length) {
        return 576;
      }
      return bands[bandIndex];
    }

    private int[] getScaleFactorBands(int blockType) {
      // Block types 0, 1, 3 use long bands; types 2 use short bands
      // Mixed type (type 2 with mixed_block_flag) uses long for first 8 bands
      if (blockType == 2) {
        return SCALE_FACTOR_BANDS_SHORT;
      }
      return SCALE_FACTOR_BANDS_LONG;
    }

    private void decodeBigValuesRegion(BitReader bits, int[] xr, int start, int end, HuffmanCode.CodeTable table) {
      if (table == null || start >= end) {
        return;
      }

      int i = start;
      while (i < end) {
        HuffmanCode.Node node = HuffmanCode.decode(table, bits);

        if (node == null) {
          break;
        }

        int x = node.x();
        int y = node.y();

        // Handle escape sequences: if x or y >= 16, read extra linbits
        if (table.linbits() > 0) {
          if (x == 15) {
            x += bits.get(table.linbits());
          }
          if (y == 15) {
            y += bits.get(table.linbits());
          }
        }

        // Sign handling: read 1 sign bit for each non-zero value
        if (x != 0) {
          x = bits.getBoolean() ? -x : x;
        }
        if (y != 0) {
          y = bits.getBoolean() ? -y : y;
        }

        xr[i++] = x;
        if (i < end) {
          xr[i++] = y;
        }
      }
    }

    private void decodeCount1Region(BitReader bits, int[] xr, int start) {
      // Count1 region: try to get table 32 (the count1 table), but it may not exist
      // If it doesn't exist, we skip count1 decoding
      var table = HuffmanCode.getTable(32);
      if (table == null) {
        // Count1 table not available - leave remaining values as 0
        return;
      }

      int i = start;
      while (i < 576) {
        HuffmanCode.Node node = HuffmanCode.decode(table, bits);

        if (node == null) {
          break;
        }

        // Extract v, w, x, y from the encoded values
        // count1 format: each is 0 or 1 (representing -1, 1 after sign handling)
        int v = node.x() & 1;
        int w = (node.x() >> 1) & 1;
        int x = node.y() & 1;
        int y = (node.y() >> 1) & 1;

        // Convert to -1, 0, 1 and apply signs
        int[] values = {
            v == 0 ? 0 : (bits.getBoolean() ? -1 : 1),
            w == 0 ? 0 : (bits.getBoolean() ? -1 : 1),
            x == 0 ? 0 : (bits.getBoolean() ? -1 : 1),
            y == 0 ? 0 : (bits.getBoolean() ? -1 : 1)
        };

        xr[i++] = values[0];
        if (i < 576) xr[i++] = values[1];
        if (i < 576) xr[i++] = values[2];
        if (i < 576) xr[i++] = values[3];
      }
    }

    public float[][][] getSamples() {
      return this.samples;
    }

    /**
     * Dequantize the Huffman-decoded samples using scale factors and global gain.
     * This converts the quantized integer values to floating-point frequency coefficients.
     * Formula: xr[i] = sign(is[i]) * 2^(-0.25 * (|is[i]| + 210) + global_gain - 210 - subblock_gain - 0.5 * scalefac_scale * scalefactor - preflag)
     */
    private void dequantize(int gr, int ch) {
      var sideInfo = this.frame.getSideInfo();
      var granule = sideInfo.channels[ch].granules[gr];
      int blockType = granule.block_type;
      boolean isShortBlock = blockType == SideInfo.Granule.BLOCK_TYPE_3_SHORT_WINDOWS;
      boolean isMixedBlock = granule.mixed_block_flag;

      for (int i = 0; i < 576; i++) {
        float is = this.samples[ch][gr][i];
        if (is == 0) {
          continue;
        }

        // Determine scale factor band and window for this frequency line
        int sfb;
        int window = 0;
        if (isShortBlock) {
          // For short blocks, determine window and sfb
          sfb = getScaleFactorBandShort(i);
          if (isMixedBlock) {
            // Mixed blocks: first 8 lines are long, rest are short
            if (i < 36) {
              sfb = getScaleFactorBandLong(i);
            }
          }
          window = getWindowForShortBlock(i);
        } else {
          // Long blocks
          sfb = getScaleFactorBandLong(i);
        }

        // Get the scale factor value
        int scalefactor;
        if (isShortBlock) {
          scalefactor = this.scaleFactors[ch][gr].s[window][Math.min(sfb, 12)];
        } else {
          scalefactor = this.scaleFactors[ch][gr].l[Math.min(sfb, 22)];
        }

        // Calculate the exponent components
        double exponent = -0.25 * (Math.abs(is) + 210);
        exponent += granule.global_gain - 210;

        // Add subblock gain for short blocks
        if (isShortBlock) {
          exponent -= granule.subblock_gain[window] * 8;
        }

        // Add scalefactor contribution
        double scalefacMultiplier = granule.scalefac_scale ? 2.0 : 1.0;
        exponent -= 0.5 * scalefacMultiplier * scalefactor;

        // Add preflag for high frequencies (not used for short blocks)
        if (granule.preflag && !isShortBlock && sfb >= 11 && sfb < PREFACTORS.length) {
          exponent -= PREFACTORS[sfb];
        }

        // Apply sign and compute final value
        float xr = (float) Math.pow(2, exponent);
        this.samples[ch][gr][i] = (is < 0) ? -xr : xr;
      }
    }

    private int getScaleFactorBandLong(int frequencyLine) {
      for (int i = 0; i < SCALE_FACTOR_BANDS_LONG.length - 1; i++) {
        if (frequencyLine < SCALE_FACTOR_BANDS_LONG[i + 1]) {
          return Math.min(i, 21); // Clamp to valid range (0-21 for l[])
        }
      }
      return 21;
    }

    private int getScaleFactorBandShort(int frequencyLine) {
      for (int i = 0; i < SCALE_FACTOR_BANDS_SHORT.length - 1; i++) {
        if (frequencyLine < SCALE_FACTOR_BANDS_SHORT[i + 1]) {
          return Math.min(i, 12); // Clamp to valid range (0-12 for s[][])
        }
      }
      return 12;
    }

    private int getWindowForShortBlock(int frequencyLine) {
      // Short blocks have 3 windows, each with 12 scale factor bands
      // Each window covers 1/3 of the frequency lines
      int windowSize = 576 / 3;
      return Math.min(frequencyLine / windowSize, 2);
    }

    private void decodeScaleFactors(BitReader bits, ScaleFactors[][] scaleFactors, int gr, int ch, SideInfo sideInfo) {
      final int SHORT_SWITCH_POINT = 6;
      final int LONG_SWITCH_POINT = 12;
      final var granule = sideInfo.channels[ch].granules[gr];
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
          var scaleFactor = sideInfo.reuseScaleFactor(gr, ch, sfb) ? scaleFactors[ch][0].l[sfb] : bits.get(bitsToRead);
          scaleFactors[ch][gr].l[sfb] = scaleFactor;
        }
      }
    }

    static class ScaleFactors {
      final int[] l = new int[23];         /* [cb] */
      final int[][] s = new int[3][13];         /* [window][cb] */
    }
  }
}
