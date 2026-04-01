package de.gurkenlabs.litiengine.sound.spi.mp3;

import org.junit.jupiter.api.Test;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.spi.BitReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Diagnostic test to trace through the MP3 decoding pipeline
 * and identify where values diverge from the expected output.
 */
class Mp3DiagnosticTest {

  @Test
  void traceFirstFrameDecoding() throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("=== MP3 Diagnostic Test ===\n\n");

    // Load MP3 data
    var mp3Url = Resources.getLocation("de/gurkenlabs/litiengine/resources/sample.mp3");
    byte[] mp3Data;
    try (var is = mp3Url.openStream()) {
      mp3Data = is.readAllBytes();
    }
    sb.append("MP3 file size: " + mp3Data.length + " bytes\n\n");

    // Skip ID3v2 if present
    int frameOffset = 0;
    if (mp3Data.length >= 10 && mp3Data[0] == 'I' && mp3Data[1] == 'D' && mp3Data[2] == '3') {
      int id3Size = ((mp3Data[6] & 0x7f) << 21) | ((mp3Data[7] & 0x7f) << 14) |
                    ((mp3Data[8] & 0x7f) << 7) | (mp3Data[9] & 0x7f);
      frameOffset = 10 + id3Size;
      sb.append("ID3v2 tag found, size: " + id3Size + " bytes\n");
    }

    // Find first frame sync
    while (frameOffset < mp3Data.length - 4) {
      if ((mp3Data[frameOffset] & 0xFF) == 0xFF && (mp3Data[frameOffset + 1] & 0xE0) == 0xE0) {
        break;
      }
      frameOffset++;
    }
    sb.append("First frame at offset: " + frameOffset + "\n");

    // Parse header
    int header = ((mp3Data[frameOffset] & 0xFF) << 24) |
                 ((mp3Data[frameOffset + 1] & 0xFF) << 16) |
                 ((mp3Data[frameOffset + 2] & 0xFF) << 8) |
                 (mp3Data[frameOffset + 3] & 0xFF);

    sb.append("\n--- Header Analysis ---\n");
    sb.append("Header bytes: " + String.format("%08X", header) + "\n");

    int protection = (header >> 16) & 0x1;
    int mode = (header >> 6) & 0x3;
    int channels = (mode == 3) ? 1 : 2;
    sb.append("Mode: " + mode + " (" + (mode == 3 ? "Mono" : "Stereo") + ")\n");
    sb.append("Channels: " + channels + "\n");

    // Side info
    int sideInfoStart = frameOffset + 4 + (protection == 0 ? 2 : 0);
    sb.append("\n--- Side Info ---\n");
    sb.append("Side info starts at: " + sideInfoStart + "\n");

    // Show raw bytes
    sb.append("Raw side info bytes:\n");
    for (int i = 0; i < Math.min(20, mp3Data.length - sideInfoStart); i++) {
      sb.append(String.format("  [%2d] = 0x%02X\n", i, mp3Data[sideInfoStart + i] & 0xFF));
    }

    // Use BitReader like the actual code does
    int sideInfoLength = (channels == 1) ? 17 : 32;
    byte[] sideInfoBytes = new byte[sideInfoLength];
    System.arraycopy(mp3Data, sideInfoStart, sideInfoBytes, 0, Math.min(sideInfoLength, mp3Data.length - sideInfoStart));

    BitReader bits = new BitReader(sideInfoBytes);
    int mainDataBegin = bits.get(9);
    sb.append("\nMain data begin: " + mainDataBegin + "\n");

    int privateBits = bits.get(channels == 1 ? 5 : 3);
    sb.append("Private bits: " + privateBits + "\n");

    // SCFSI
    boolean[] scfsi = new boolean[4];
    scfsi[0] = bits.getBoolean();
    scfsi[1] = bits.getBoolean();
    scfsi[2] = bits.getBoolean();
    scfsi[3] = bits.getBoolean();
    sb.append("SCFSI[0-3]: " + scfsi[0] + ", " + scfsi[1] + ", " + scfsi[2] + ", " + scfsi[3] + "\n");

    // Granule 0
    sb.append("\n--- Granule 0, Channel 0 ---\n");
    int part2_3_length = bits.get(12);
    sb.append("part2_3_length: " + part2_3_length + "\n");

    int big_values = bits.get(9);
    sb.append("big_values: " + big_values + "\n");

    int global_gain = bits.get(8);
    sb.append("global_gain: " + global_gain + "\n");

    double g_gain = Math.pow(2, 0.25 * (global_gain - 210.0));
    sb.append("g_gain: " + g_gain + "\n");

    int scalefac_compress = bits.get(4);
    sb.append("scalefac_compress: " + scalefac_compress + "\n");

    boolean window_switching = bits.getBoolean();
    sb.append("window_switching_flag: " + window_switching + "\n");

    if (window_switching) {
      int block_type = bits.get(2);
      boolean mixed_block = bits.getBoolean();
      sb.append("block_type: " + block_type + "\n");
      sb.append("mixed_block_flag: " + mixed_block + "\n");

      int table0 = bits.get(5);
      int table1 = bits.get(5);
      sb.append("table_select[0]: " + table0 + "\n");
      sb.append("table_select[1]: " + table1 + "\n");

      int subblock0 = bits.get(3);
      int subblock1 = bits.get(3);
      int subblock2 = bits.get(3);
      sb.append("subblock_gain[0-2]: " + subblock0 + ", " + subblock1 + ", " + subblock2 + "\n");
    } else {
      int table0 = bits.get(5);
      int table1 = bits.get(5);
      int table2 = bits.get(5);
      sb.append("table_select[0]: " + table0 + "\n");
      sb.append("table_select[1]: " + table1 + "\n");
      sb.append("table_select[2]: " + table2 + "\n");

      int region0_count = bits.get(4);
      int region1_count = bits.get(3);
      sb.append("region0_count: " + region0_count + "\n");
      sb.append("region1_count: " + region1_count + "\n");
    }

    boolean preflag = bits.getBoolean();
    boolean scalefac_scale = bits.getBoolean();
    int count1table = bits.get(1);
    sb.append("preflag: " + preflag + "\n");
    sb.append("scalefac_scale: " + scalefac_scale + "\n");
    sb.append("count1table_select: " + count1table + "\n");

    // Compare with reference
    sb.append("\n=== Comparing with reference ===\n");
    
    var bais = new java.io.ByteArrayInputStream(mp3Data);
    var refStream = javax.sound.sampled.AudioSystem.getAudioInputStream(bais);
    var refFormat = refStream.getFormat();
    var decodedFormat = new javax.sound.sampled.AudioFormat(
        javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
        refFormat.getSampleRate(), 16, refFormat.getChannels(),
        refFormat.getChannels() * 2, refFormat.getSampleRate(), false);
    var pcmStream = javax.sound.sampled.AudioSystem.getAudioInputStream(decodedFormat, refStream);
    byte[] refData = pcmStream.readAllBytes();
    pcmStream.close(); refStream.close();
    
    // Get our output
    var sound = Resources.sounds().get("de/gurkenlabs/litiengine/resources/sample.mp3");
    byte[] ourData = sound.getStreamData();
    
    sb.append("Reference samples: " + (refData.length/2) + "\n");
    sb.append("Our samples: " + (ourData.length/2) + "\n");
    
    // First 20 samples comparison
    sb.append("\nFirst 20 samples:\n");
    ByteBuffer ourBuf = ByteBuffer.wrap(ourData).order(ByteOrder.LITTLE_ENDIAN);
    ByteBuffer refBuf = ByteBuffer.wrap(refData).order(ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 20; i++) {
      sb.append("  ").append(i).append(": ref=").append(refBuf.getShort()).append(" our=").append(ourBuf.getShort()).append("\n");
    }
    
    // Write to file - use absolute path for reliability
    String userDir = System.getProperty("user.dir");
    Path outPath = Paths.get(userDir, "litiengine", "build", "mp3-diagnostic.txt");
    Files.writeString(outPath, sb.toString());
    System.out.println("Diagnostic written to " + outPath.toAbsolutePath());
  }
}
