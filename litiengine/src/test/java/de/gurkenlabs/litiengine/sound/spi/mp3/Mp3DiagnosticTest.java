package de.gurkenlabs.litiengine.sound.spi.mp3;

import org.junit.jupiter.api.Test;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.spi.BitReader;

import java.nio.ByteBuffer;

/**
 * Diagnostic test to trace through the MP3 decoding pipeline
 * and identify where values diverge from the expected output.
 */
class Mp3DiagnosticTest {

  @Test
  void traceFirstFrameDecoding() throws Exception {
    System.out.println("=== MP3 Diagnostic Test ===\n");

    // Load MP3 data
    var mp3Url = Resources.getLocation("de/gurkenlabs/litiengine/resources/sample.mp3");
    byte[] mp3Data;
    try (var is = mp3Url.openStream()) {
      mp3Data = is.readAllBytes();
    }
    System.out.println("MP3 file size: " + mp3Data.length + " bytes\n");

    // Skip ID3v2 if present
    int frameOffset = 0;
    if (mp3Data.length >= 10 && mp3Data[0] == 'I' && mp3Data[1] == 'D' && mp3Data[2] == '3') {
      int id3Size = ((mp3Data[6] & 0x7f) << 21) | ((mp3Data[7] & 0x7f) << 14) |
                    ((mp3Data[8] & 0x7f) << 7) | (mp3Data[9] & 0x7f);
      frameOffset = 10 + id3Size;
      System.out.println("ID3v2 tag found, size: " + id3Size + " bytes");
    }

    // Find first frame sync
    while (frameOffset < mp3Data.length - 4) {
      if ((mp3Data[frameOffset] & 0xFF) == 0xFF && (mp3Data[frameOffset + 1] & 0xE0) == 0xE0) {
        break;
      }
      frameOffset++;
    }
    System.out.println("First frame at offset: " + frameOffset);

    // Parse header
    int header = ((mp3Data[frameOffset] & 0xFF) << 24) |
                 ((mp3Data[frameOffset + 1] & 0xFF) << 16) |
                 ((mp3Data[frameOffset + 2] & 0xFF) << 8) |
                 (mp3Data[frameOffset + 3] & 0xFF);

    System.out.println("\n--- Header Analysis ---");
    System.out.println("Header bytes: " + String.format("%08X", header));

    int protection = (header >> 16) & 0x1;
    int mode = (header >> 6) & 0x3;
    int channels = (mode == 3) ? 1 : 2;
    System.out.println("Mode: " + mode + " (" + (mode == 3 ? "Mono" : "Stereo") + ")");
    System.out.println("Channels: " + channels);

    // Side info
    int sideInfoStart = frameOffset + 4 + (protection == 0 ? 2 : 0);
    System.out.println("\n--- Side Info ---");
    System.out.println("Side info starts at: " + sideInfoStart);

    // Show raw bytes
    System.out.println("Raw side info bytes:");
    for (int i = 0; i < Math.min(20, mp3Data.length - sideInfoStart); i++) {
      System.out.printf("  [%2d] = 0x%02X\n", i, mp3Data[sideInfoStart + i] & 0xFF);
    }

    // Use BitReader like the actual code does
    int sideInfoLength = (channels == 1) ? 17 : 32;
    byte[] sideInfoBytes = new byte[sideInfoLength];
    System.arraycopy(mp3Data, sideInfoStart, sideInfoBytes, 0, Math.min(sideInfoLength, mp3Data.length - sideInfoStart));

    BitReader bits = new BitReader(sideInfoBytes);
    int mainDataBegin = bits.get(9);
    System.out.println("\nMain data begin: " + mainDataBegin);

    int privateBits = bits.get(channels == 1 ? 5 : 3);
    System.out.println("Private bits: " + privateBits);

    // SCFSI
    boolean[] scfsi = new boolean[4];
    scfsi[0] = bits.getBoolean();
    scfsi[1] = bits.getBoolean();
    scfsi[2] = bits.getBoolean();
    scfsi[3] = bits.getBoolean();
    System.out.println("SCFSI[0-3]: " + scfsi[0] + ", " + scfsi[1] + ", " + scfsi[2] + ", " + scfsi[3]);

    // Granule 0
    System.out.println("\n--- Granule 0, Channel 0 ---");
    int part2_3_length = bits.get(12);
    System.out.println("part2_3_length: " + part2_3_length);

    int big_values = bits.get(9);
    System.out.println("big_values: " + big_values);

    int global_gain = bits.get(8);
    System.out.println("global_gain: " + global_gain);

    double g_gain = Math.pow(2, 0.25 * (global_gain - 210.0));
    System.out.println("g_gain: " + g_gain);

    int scalefac_compress = bits.get(4);
    System.out.println("scalefac_compress: " + scalefac_compress);

    boolean window_switching = bits.getBoolean();
    System.out.println("window_switching_flag: " + window_switching);

    if (window_switching) {
      int block_type = bits.get(2);
      boolean mixed_block = bits.getBoolean();
      System.out.println("block_type: " + block_type);
      System.out.println("mixed_block_flag: " + mixed_block);

      int table0 = bits.get(5);
      int table1 = bits.get(5);
      System.out.println("table_select[0]: " + table0);
      System.out.println("table_select[1]: " + table1);

      int subblock0 = bits.get(3);
      int subblock1 = bits.get(3);
      int subblock2 = bits.get(3);
      System.out.println("subblock_gain[0-2]: " + subblock0 + ", " + subblock1 + ", " + subblock2);
    } else {
      int table0 = bits.get(5);
      int table1 = bits.get(5);
      int table2 = bits.get(5);
      System.out.println("table_select[0]: " + table0);
      System.out.println("table_select[1]: " + table1);
      System.out.println("table_select[2]: " + table2);

      int region0_count = bits.get(4);
      int region1_count = bits.get(3);
      System.out.println("region0_count: " + region0_count);
      System.out.println("region1_count: " + region1_count);
    }

    boolean preflag = bits.getBoolean();
    boolean scalefac_scale = bits.getBoolean();
    int count1table = bits.get(1);
    System.out.println("preflag: " + preflag);
    System.out.println("scalefac_scale: " + scalefac_scale);
    System.out.println("count1table_select: " + count1table);

    System.out.println("\n--- Investigating Frame Position ---");
    // Show bytes around the frame offset
    System.out.println("Bytes around frame offset " + frameOffset + ":");
    for (int i = -5; i <= 25; i++) {
      int pos = frameOffset + i;
      if (pos >= 0 && pos < mp3Data.length) {
        byte b = mp3Data[pos];
        char c = (b >= 32 && b < 127) ? (char) b : '.';
        System.out.printf("  [%4d] = 0x%02X '%c'%s\n", pos, b & 0xFF, c,
            (pos == frameOffset) ? " <-- FRAME START" : "");
      }
    }

    // Look for frame sync pattern
    System.out.println("\n--- Looking for Frame Sync Pattern ---");
    int syncCount = 0;
    for (int i = Math.max(0, frameOffset - 100); i < Math.min(mp3Data.length - 4, frameOffset + 200); i++) {
      if ((mp3Data[i] & 0xFF) == 0xFF && (mp3Data[i + 1] & 0xE0) == 0xE0) {
        int headerVal = ((mp3Data[i] & 0xFF) << 24) | ((mp3Data[i + 1] & 0xFF) << 16) |
                       ((mp3Data[i + 2] & 0xFF) << 8) | (mp3Data[i + 3] & 0xFF);
        int bitrateIdx = (headerVal >> 12) & 0xF;
        int srIdx = (headerVal >> 10) & 0x3;
        int headerMode = (headerVal >> 6) & 0x3;
        System.out.printf("  Offset %d: header=%08X bitrateIdx=%d srIdx=%d mode=%d\n",
            i, headerVal, bitrateIdx, srIdx, headerMode);
        syncCount++;
        if (syncCount >= 10) break;
      }
    }

    System.out.println("\n=== Root Cause Analysis ===");
    System.out.println("The frame at offset " + frameOffset + " is NOT a real audio frame!");
    System.out.println("It's an Xing/Info header frame with metadata.");
    System.out.println("The side info being all zeros is correct for Xing/Info frames.");
    System.out.println("\nLook for the NEXT frame sync after this one for the real audio data.");

    // Find the next frame after the Xing header
    int nextFrameStart = frameOffset + 4 + 17; // header + side info
    // Skip past the Xing/Info data
    while (nextFrameStart < mp3Data.length - 4) {
      if ((mp3Data[nextFrameStart] & 0xFF) == 0xFF && (mp3Data[nextFrameStart + 1] & 0xE0) == 0xE0) {
        break;
      }
      nextFrameStart++;
    }
    System.out.println("Next frame sync at: " + nextFrameStart);

    // Show bytes at the next frame
    System.out.println("\nBytes around next frame:");
    for (int i = -2; i <= 25; i++) {
      int pos = nextFrameStart + i;
      if (pos >= 0 && pos < mp3Data.length) {
        byte b = mp3Data[pos];
        char c = (b >= 32 && b < 127) ? (char) b : '.';
        System.out.printf("  [%4d] = 0x%02X '%c'%s\n", pos, b & 0xFF, c,
            (pos == nextFrameStart) ? " <-- NEXT FRAME" : "");
      }
    }

    // Parse side info from the NEXT frame
    System.out.println("\n--- Side Info from NEXT frame ---");
    int nextSideInfoStart = nextFrameStart + 4; // No CRC
    byte[] nextSideInfo = new byte[17];
    System.arraycopy(mp3Data, nextSideInfoStart, nextSideInfo, 0, 17);

    System.out.println("Raw side info bytes:");
    for (int i = 0; i < 17; i++) {
      System.out.printf("  [%2d] = 0x%02X\n", i, nextSideInfo[i] & 0xFF);
    }

    BitReader nextBits = new BitReader(nextSideInfo);
    int nextMainDataBegin = nextBits.get(9);
    System.out.println("\nMain data begin: " + nextMainDataBegin);

    int nextPrivateBits = nextBits.get(5);
    System.out.println("Private bits: " + nextPrivateBits);

    boolean[] nextScfsi = new boolean[4];
    nextScfsi[0] = nextBits.getBoolean();
    nextScfsi[1] = nextBits.getBoolean();
    nextScfsi[2] = nextBits.getBoolean();
    nextScfsi[3] = nextBits.getBoolean();
    System.out.println("SCFSI[0-3]: " + nextScfsi[0] + ", " + nextScfsi[1] + ", " + nextScfsi[2] + ", " + nextScfsi[3]);

    System.out.println("\nGranule 0 from NEXT frame:");
    int nextPart2_3 = nextBits.get(12);
    System.out.println("  part2_3_length: " + nextPart2_3);

    int nextBigValues = nextBits.get(9);
    System.out.println("  big_values: " + nextBigValues);

    int nextGlobalGain = nextBits.get(8);
    System.out.println("  global_gain: " + nextGlobalGain);
    System.out.println("  g_gain: " + Math.pow(2, 0.25 * (nextGlobalGain - 210.0)));
  }
}
