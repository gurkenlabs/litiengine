package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.resources.Resources;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Diagnostic test to trace MP3 decoding pipeline step by step.
 */
class Mp3DecoderDiagnosticTest {

  private static final String MP3_RESOURCE = "de/gurkenlabs/litiengine/resources/sample.mp3";

  @Test
  void traceDecodingPipeline() throws Exception {
    System.out.println("=== MP3 Decoder Diagnostic ===\n");

    // Load MP3 data
    InputStream mp3Stream = Resources.getLocation(MP3_RESOURCE).openStream();
    byte[] mp3Data = mp3Stream.readAllBytes();
    mp3Stream.close();
    System.out.println("MP3 file size: " + mp3Data.length + " bytes\n");

    // Skip ID3v2 if present
    int mp3Position = 0;
    if (mp3Data.length >= 10 && mp3Data[0] == 'I' && mp3Data[1] == 'D' && mp3Data[2] == '3') {
      int id3Size = ((mp3Data[6] & 0x7f) << 21) | ((mp3Data[7] & 0x7f) << 14) |
                    ((mp3Data[8] & 0x7f) << 7) | (mp3Data[9] & 0x7f);
      mp3Position = 10 + id3Size;
      System.out.println("ID3v2 tag skipped: " + id3Size + " bytes\n");
    }

    // Find first frame sync
    int frameStart = mp3Position;
    while (frameStart < mp3Data.length - 4) {
      if ((mp3Data[frameStart] & 0xFF) == 0xFF && (mp3Data[frameStart + 1] & 0xE0) == 0xE0) {
        break;
      }
      frameStart++;
    }
    System.out.println("First frame sync at: " + frameStart);

    // Parse and decode first 5 frames
    ByteBuffer buffer = ByteBuffer.wrap(mp3Data);
    int position = frameStart;
    
    // Bit reservoir for proper decoding
    byte[] reservoir = new byte[4096];
    int reservoirWritePos = 0;
    int reservoirTotalWritten = 0;

    for (int frameNum = 0; frameNum < 5 && position < mp3Data.length - 4; frameNum++) {
      System.out.println("\n--- Frame " + frameNum + " at offset " + position + " ---");
      
      // Parse header
      int header = ((mp3Data[position] & 0xFF) << 24) | ((mp3Data[position + 1] & 0xFF) << 16) |
                   ((mp3Data[position + 2] & 0xFF) << 8) | (mp3Data[position + 3] & 0xFF);
      
      int version = (header >> 19) & 0x3;
      int layer = (header >> 17) & 0x3;
      int bitrateIndex = (header >> 12) & 0xF;
      int sampleRateIndex = (header >> 10) & 0x3;
      int padding = (header >> 9) & 0x1;
      int mode = (header >> 6) & 0x3;
      int protection = (header >> 16) & 0x1;

      System.out.println("  Version: " + version + " (1=MPEG1, 2=MPEG2)");
      System.out.println("  Layer: " + layer);
      System.out.println("  Mode: " + mode + " (0=Stereo, 1=Joint, 2=Dual, 3=Mono)");
      System.out.println("  Bitrate index: " + bitrateIndex);
      System.out.println("  Sample rate index: " + sampleRateIndex);

      int[] bitrates = {0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 0};
      int bitrate = bitrates[bitrateIndex] * 1000;
      
      int[][] sampleRates = {
        {11025, 12000, 8000, 0},
        {0, 0, 0, 0},
        {22050, 24000, 16000, 0},
        {44100, 48000, 32000, 0}
      };
      int sampleRate = sampleRates[version][sampleRateIndex];

      int frameSize = (144 * bitrate / sampleRate) + padding;
      System.out.println("  Frame size: " + frameSize + " bytes");

      // Parse side info
      int channels = (mode == 3) ? 1 : 2;
      int sideInfoSize = (channels == 1) ? 17 : 32;
      int sideInfoOffset = position + 4 + (protection == 0 ? 2 : 0);

      // Read mainDataBegin
      int mainDataBegin = ((mp3Data[sideInfoOffset] & 0xFF) << 1) | ((mp3Data[sideInfoOffset + 1] >> 7) & 1);
      System.out.println("  mainDataBegin: " + mainDataBegin);

      // Skip Xing/Info frames
      int markerOffset = position + 4 + sideInfoSize;
      if (markerOffset + 4 <= mp3Data.length) {
        String marker = new String(mp3Data, markerOffset, 4);
        if ("Xing".equals(marker) || "Info".equals(marker)) {
          System.out.println("  Xing/Info header - skipping");
          position += frameSize;
          continue;
        }
      }

      // Handle bit reservoir
      int mainDataSize = frameSize - 4 - (protection == 0 ? 2 : 0) - sideInfoSize;
      byte[] frameMainData;
      
      if (mainDataBegin == 0) {
        // Read from current frame
        frameMainData = new byte[mainDataSize];
        System.arraycopy(mp3Data, position + 4 + (protection == 0 ? 2 : 0) + sideInfoSize, frameMainData, 0, mainDataSize);
        // Add to reservoir
        for (int i = 0; i < mainDataSize; i++) {
          reservoir[reservoirWritePos] = frameMainData[i];
          reservoirWritePos = (reservoirWritePos + 1) % 4096;
        }
        reservoirTotalWritten += mainDataSize;
      } else {
        // Read from reservoir
        if (mainDataBegin > reservoirTotalWritten) {
          System.out.println("  Skipping - not enough data in reservoir (have " + reservoirTotalWritten + ", need " + mainDataBegin + ")");
          position += frameSize;
          continue;
        }
        frameMainData = new byte[mainDataSize];
        // Read mainDataSize bytes starting mainDataBegin bytes back from current write position
        // The data we want starts at: writePos - mainDataBegin - mainDataSize
        int startPos = (reservoirWritePos - mainDataBegin - mainDataSize + 8192) % 4096;
        for (int i = 0; i < mainDataSize; i++) {
          frameMainData[i] = reservoir[(startPos + i) % 4096];
        }
        System.out.println("  Reservoir read: startPos=" + startPos + " (writePos=" + reservoirWritePos + " - mainDataBegin=" + mainDataBegin + " - mainDataSize=" + mainDataSize + ")");
        
        // Add current frame's data to reservoir
        for (int i = 0; i < mainDataSize; i++) {
          reservoir[reservoirWritePos] = mp3Data[position + 4 + (protection == 0 ? 2 : 0) + sideInfoSize + i];
          reservoirWritePos = (reservoirWritePos + 1) % 4096;
        }
        reservoirTotalWritten += mainDataSize;
      }

      // Check if all zeros
      boolean allZeros = true;
      for (byte b : frameMainData) {
        if (b != 0) {
          allZeros = false;
          break;
        }
      }
      
      System.out.println("  Main data: " + mainDataSize + " bytes, allZeros=" + allZeros);
      if (!allZeros && mainDataSize > 0) {
        System.out.println("  First 4 bytes: " + String.format("%02X %02X %02X %02X", 
            frameMainData[0] & 0xFF, frameMainData[1] & 0xFF, frameMainData[2] & 0xFF, frameMainData[3] & 0xFF));
      }

      position += frameSize;
    }

    System.out.println("\n=== End Diagnostic ===");
  }

  @Test
  void compareWithReference() throws Exception {
    System.out.println("=== Reference Comparison ===\n");

    // Load MP3
    InputStream mp3Stream = Resources.getLocation(MP3_RESOURCE).openStream();
    byte[] mp3Data = mp3Stream.readAllBytes();
    mp3Stream.close();

    // Decode with reference (mp3spi)
    byte[] referencePcm = decodeWithMp3Spi(mp3Data);
    System.out.println("Reference PCM: " + referencePcm.length + " bytes");

    // Decode with our implementation
    ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data);
    Mp3FileReader reader = new Mp3FileReader();
    AudioInputStream mp3Stream2 = reader.getAudioInputStream(bais);
    
    AudioFormat baseFormat = mp3Stream2.getFormat();
    AudioFormat decodedFormat = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        baseFormat.getSampleRate(),
        16,
        baseFormat.getChannels(),
        baseFormat.getChannels() * 2,
        baseFormat.getSampleRate(),
        false
    );

    AudioInputStream pcmStream = AudioSystem.getAudioInputStream(decodedFormat, mp3Stream2);
    byte[] ourPcm = pcmStream.readAllBytes();
    pcmStream.close();
    mp3Stream2.close();

    System.out.println("Our PCM: " + ourPcm.length + " bytes");

    // Find first non-zero in each
    int refFirstNonZero = findFirstNonZero(referencePcm);
    int ourFirstNonZero = findFirstNonZero(ourPcm);

    System.out.println("\nFirst non-zero sample:");
    System.out.println("  Reference: " + refFirstNonZero);
    System.out.println("  Ours: " + ourFirstNonZero);
    System.out.println("  Difference: " + (ourFirstNonZero - refFirstNonZero) + " samples");

    // Show samples around first non-zero
    if (refFirstNonZero >= 0) {
      System.out.println("\nReference samples around first non-zero:");
      showSamples(referencePcm, refFirstNonZero - 5, refFirstNonZero + 10);
    }

    if (ourFirstNonZero >= 0) {
      System.out.println("\nOur samples around first non-zero:");
      showSamples(ourPcm, ourFirstNonZero - 5, ourFirstNonZero + 10);
    }
  }

  private byte[] decodeWithMp3Spi(byte[] mp3Data) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data);
    AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(bais);

    AudioFormat baseFormat = mp3Stream.getFormat();
    AudioFormat decodedFormat = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        baseFormat.getSampleRate(),
        16,
        baseFormat.getChannels(),
        baseFormat.getChannels() * 2,
        baseFormat.getSampleRate(),
        false
    );

    AudioInputStream pcmStream = AudioSystem.getAudioInputStream(decodedFormat, mp3Stream);
    byte[] pcmData = pcmStream.readAllBytes();
    pcmStream.close();
    mp3Stream.close();

    return pcmData;
  }

  private int findFirstNonZero(byte[] pcmData) {
    ByteBuffer buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < pcmData.length / 2; i++) {
      if (buffer.getShort() != 0) {
        return i;
      }
    }
    return -1;
  }

  private void showSamples(byte[] pcmData, int start, int end) {
    ByteBuffer buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN);
    int numSamples = pcmData.length / 2;
    
    start = Math.max(0, start);
    end = Math.min(numSamples, end);

    // Skip to start
    for (int i = 0; i < start; i++) {
      buffer.getShort();
    }

    for (int i = start; i < end; i++) {
      short sample = buffer.getShort();
      System.out.printf("  Sample %4d: %6d\n", i, sample);
    }
  }
}
