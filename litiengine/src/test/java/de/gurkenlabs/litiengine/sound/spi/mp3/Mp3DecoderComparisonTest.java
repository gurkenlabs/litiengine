package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.resources.Resources;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to compare MP3 decoding output between our implementation and the reference mp3spi library.
 * This helps identify where our decoder differs from the standard implementation.
 */
class Mp3DecoderComparisonTest {

  private static final String MP3_RESOURCE = "de/gurkenlabs/litiengine/resources/sample.mp3";

  @Test
  void compareDecodedOutput() throws IOException, UnsupportedAudioFileException {
    // Load the MP3 file
    InputStream mp3Stream = Resources.getLocation(MP3_RESOURCE).openStream();
    byte[] mp3Data = mp3Stream.readAllBytes();
    mp3Stream.close();

    System.out.println("=== MP3 Decoder Comparison Test ===");
    System.out.println("MP3 file size: " + mp3Data.length + " bytes");

    // Decode with mp3spi (reference implementation)
    byte[] referencePcm = decodeWithMp3Spi(mp3Data);
    System.out.println("\n--- mp3spi (Reference) ---");
    System.out.println("PCM data size: " + referencePcm.length + " bytes");
    printPcmStatistics(referencePcm, "Reference");
    
    // Check reference format
    ByteArrayInputStream bais2 = new ByteArrayInputStream(mp3Data);
    AudioInputStream mp3StreamRef = AudioSystem.getAudioInputStream(bais2);
    AudioFormat refFormat = mp3StreamRef.getFormat();
    System.out.println("Reference format: " + refFormat);
    mp3StreamRef.close();

    // Decode with our implementation
    byte[] ourPcm = decodeWithOurImplementation(mp3Data);
    System.out.println("\n--- Our Implementation ---");
    System.out.println("PCM data size: " + ourPcm.length + " bytes");
    printPcmStatistics(ourPcm, "Our");

    // Compare outputs
    System.out.println("\n--- Detailed Sample Comparison ---");
    System.out.println("Finding first non-zero sample in each output...");
    
    int refSamples = referencePcm.length / 2;
    int ourSamples = ourPcm.length / 2;
    int minSamples = Math.min(refSamples, ourSamples);
    
    // Find first non-zero in reference
    ByteBuffer refBufTemp = ByteBuffer.wrap(referencePcm).order(ByteOrder.LITTLE_ENDIAN);
    int refFirstNonZero = -1;
    for (int i = 0; i < refSamples; i++) {
      if (refBufTemp.getShort() != 0) {
        refFirstNonZero = i;
        break;
      }
    }
    
    // Find first non-zero in ours
    ByteBuffer ourBufTemp = ByteBuffer.wrap(ourPcm).order(ByteOrder.LITTLE_ENDIAN);
    int ourFirstNonZero = -1;
    for (int i = 0; i < ourSamples; i++) {
      if (ourBufTemp.getShort() != 0) {
        ourFirstNonZero = i;
        break;
      }
    }
    
    System.out.println("Reference first non-zero at sample: " + refFirstNonZero);
    System.out.println("Our first non-zero at sample: " + ourFirstNonZero);
    
    // Show samples around first non-zero
    if (refFirstNonZero >= 0) {
      System.out.println("\nSamples around reference's first non-zero:");
      ByteBuffer refBuf = ByteBuffer.wrap(referencePcm).order(ByteOrder.LITTLE_ENDIAN);
      ByteBuffer ourBuf = ByteBuffer.wrap(ourPcm).order(ByteOrder.LITTLE_ENDIAN);
      int start = Math.max(0, refFirstNonZero - 5);
      int end = Math.min(minSamples, refFirstNonZero + 10);
      // Skip to start position
      for (int i = 0; i < start; i++) {
        refBuf.getShort();
        ourBuf.getShort();
      }
      for (int i = start; i < end; i++) {
        short refVal = refBuf.getShort();
        short ourVal = ourBuf.getShort();
        System.out.printf("  Sample %d: ref=%6d our=%6d diff=%6d%s\n", 
            i, refVal, ourVal, Math.abs(refVal - ourVal),
            (i == refFirstNonZero) ? " <-- REF FIRST NON-ZERO" : "");
      }
    }
    
    // Show samples around our first non-zero
    if (ourFirstNonZero >= 0 && ourFirstNonZero != refFirstNonZero) {
      System.out.println("\nSamples around our first non-zero:");
      ByteBuffer refBuf = ByteBuffer.wrap(referencePcm).order(ByteOrder.LITTLE_ENDIAN);
      ByteBuffer ourBuf = ByteBuffer.wrap(ourPcm).order(ByteOrder.LITTLE_ENDIAN);
      int start = Math.max(0, ourFirstNonZero - 5);
      int end = Math.min(minSamples, ourFirstNonZero + 10);
      // Skip to start position
      for (int i = 0; i < start; i++) {
        refBuf.getShort();
        ourBuf.getShort();
      }
      for (int i = start; i < end; i++) {
        short refVal = refBuf.getShort();
        short ourVal = ourBuf.getShort();
        System.out.printf("  Sample %d: ref=%6d our=%6d diff=%6d%s\n", 
            i, refVal, ourVal, Math.abs(refVal - ourVal),
            (i == ourFirstNonZero) ? " <-- OUR FIRST NON-ZERO" : "");
      }
    }

    // Save outputs to files for manual inspection
    Path outputDir = Paths.get(System.getProperty("user.dir"), "build", "test-output");
    Files.createDirectories(outputDir);
    saveToWav(referencePcm, outputDir.resolve("reference_output.wav").toString(), 32000, 1, 16);
    saveToWav(ourPcm, outputDir.resolve("our_output.wav").toString(), 32000, 1, 16);
    // Print comparison to stdout
    System.out.println("\n=== DETAILED COMPARISON ===");
    compareOutputs(referencePcm, ourPcm);
    
    // Write results to a file
    Path resultsFile = Paths.get("litiengine", "build", "mp3-comparison-results.txt");
    Files.createDirectories(resultsFile.getParent());
    StringBuilder sb = new StringBuilder();
    sb.append("=== MP3 Decoder Comparison Results ===\n");
    sb.append("Reference samples: ").append(referencePcm.length / 2).append("\n");
    sb.append("Our samples: ").append(ourPcm.length / 2).append("\n");
    sb.append("Sample diff: ").append((referencePcm.length / 2) - (ourPcm.length / 2)).append("\n");
    sb.append("\nComparison:\n");
    
    // Quick comparison
    int compSamples = Math.min(referencePcm.length, ourPcm.length) / 2;
    long totalDiff = 0;
    int matchingSamples = 0;
    int firstMismatch = -1;
    long maxDiff = 0;
    ByteBuffer refBuf = ByteBuffer.wrap(referencePcm).order(ByteOrder.LITTLE_ENDIAN);
    ByteBuffer ourBuf = ByteBuffer.wrap(ourPcm).order(ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < compSamples; i++) {
      long diff = Math.abs((long) refBuf.getShort() - (long) ourBuf.getShort());
      totalDiff += diff;
      if (diff == 0) matchingSamples++;
      if (diff > 0 && firstMismatch == -1) firstMismatch = i;
      maxDiff = Math.max(maxDiff, diff);
    }
    double matchPct = 100.0 * matchingSamples / compSamples;
    sb.append("Matching: ").append(matchingSamples).append("/").append(compSamples).append(" (").append(String.format("%.2f", matchPct)).append("%)\n");
    sb.append("Average diff: ").append(String.format("%.2f", (double) totalDiff / compSamples)).append("\n");
    sb.append("Max diff: ").append(maxDiff).append("\n");
    sb.append("First mismatch: ").append(firstMismatch).append("\n");
    
    // Print to console
    System.out.println("=== MP3 Decoder Comparison Results ===");
    System.out.println("Reference samples: " + (referencePcm.length / 2));
    System.out.println("Our samples: " + (ourPcm.length / 2));
    System.out.println("Sample diff: " + ((referencePcm.length / 2) - (ourPcm.length / 2)));
    System.out.println("Matching: " + matchingSamples + "/" + compSamples + " (" + String.format("%.2f", matchPct) + "%)");
    System.out.println("Average diff: " + String.format("%.2f", (double) totalDiff / compSamples));
    System.out.println("Max diff: " + maxDiff);
    System.out.println("First mismatch: " + firstMismatch);
    
    Files.writeString(resultsFile, sb.toString());
    System.out.println("Results written to " + resultsFile.toAbsolutePath());
    System.out.flush();
    
    // Force output
    System.out.flush();
    System.err.println("TEST OUTPUT COMPLETE");
    
    // Basic assertions
    assertTrue(referencePcm.length > 0, "Reference PCM should not be empty");
    assertTrue(ourPcm.length > 0, "Our PCM should not be empty");
  }

  /**
   * Decode MP3 using mp3spi library (reference implementation).
   */
  private byte[] decodeWithMp3Spi(byte[] mp3Data) throws IOException, UnsupportedAudioFileException {
    ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data);
    AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(bais);

    // Get the format from mp3spi
    AudioFormat baseFormat = mp3Stream.getFormat();

    // Convert to PCM if needed
    AudioFormat decodedFormat = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        baseFormat.getSampleRate(),
        16,
        baseFormat.getChannels(),
        baseFormat.getChannels() * 2,
        baseFormat.getSampleRate(),
        false // little-endian
    );

    AudioInputStream pcmStream = AudioSystem.getAudioInputStream(decodedFormat, mp3Stream);
    byte[] pcmData = pcmStream.readAllBytes();
    pcmStream.close();
    mp3Stream.close();

    return pcmData;
  }

  /**
   * Decode MP3 using our implementation.
   */
  private byte[] decodeWithOurImplementation(byte[] mp3Data) throws IOException, UnsupportedAudioFileException {
    // Use our Mp3AudioInputStream
    ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data);
    Mp3FileReader reader = new Mp3FileReader();
    AudioInputStream mp3Stream = reader.getAudioInputStream(bais);

    // Get the format
    AudioFormat baseFormat = mp3Stream.getFormat();

    // Convert to PCM format matching mp3spi output
    AudioFormat decodedFormat = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        baseFormat.getSampleRate(),
        16,
        baseFormat.getChannels(),
        baseFormat.getChannels() * 2,
        baseFormat.getSampleRate(),
        false // little-endian
    );

    AudioInputStream pcmStream = AudioSystem.getAudioInputStream(decodedFormat, mp3Stream);
    byte[] pcmData = pcmStream.readAllBytes();
    pcmStream.close();
    mp3Stream.close();

    return pcmData;
  }

  /**
   * Print statistics about PCM data.
   */
  private void printPcmStatistics(byte[] pcmData, String label) {
    if (pcmData.length < 2) {
      System.out.println(label + ": No data to analyze");
      return;
    }

    int numSamples = pcmData.length / 2;
    short minSample = Short.MAX_VALUE;
    short maxSample = Short.MIN_VALUE;
    long sum = 0;
    int nonZeroCount = 0;

    ByteBuffer buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < numSamples; i++) {
      short sample = buffer.getShort();
      minSample = (short) Math.min(minSample, sample);
      maxSample = (short) Math.max(maxSample, sample);
      sum += Math.abs(sample);
      if (sample != 0) {
        nonZeroCount++;
      }
    }

    double avgAmplitude = numSamples > 0 ? (double) sum / numSamples : 0;

    System.out.println(label + " Statistics:");
    System.out.println("  Total samples: " + numSamples);
    System.out.println("  Non-zero samples: " + nonZeroCount + " (" + String.format("%.1f%%", 100.0 * nonZeroCount / numSamples) + ")");
    System.out.println("  Min sample: " + minSample);
    System.out.println("  Max sample: " + maxSample);
    System.out.println("  Avg amplitude: " + String.format("%.2f", avgAmplitude));

    // Print first 10 samples
    System.out.print("  First 10 samples: [");
    buffer.rewind();
    for (int i = 0; i < Math.min(10, numSamples); i++) {
      if (i > 0) System.out.print(", ");
      System.out.print(buffer.getShort());
    }
    System.out.println("]");
  }

  /**
   * Compare two PCM outputs and report differences.
   */
  private void compareOutputs(byte[] reference, byte[] ours) {
    int refSamples = reference.length / 2;
    int ourSamples = ours.length / 2;
    int minSamples = Math.min(refSamples, ourSamples);

    System.out.println("Reference samples: " + refSamples);
    System.out.println("Our samples: " + ourSamples);
    System.out.println("Samples to compare: " + minSamples);

    if (minSamples == 0) {
      System.out.println("Cannot compare - no samples available");
      return;
    }

    ByteBuffer refBuffer = ByteBuffer.wrap(reference).order(ByteOrder.LITTLE_ENDIAN);
    ByteBuffer ourBuffer = ByteBuffer.wrap(ours).order(ByteOrder.LITTLE_ENDIAN);

    long totalDiff = 0;
    int matchingSamples = 0;
    int firstMismatch = -1;
    long maxDiff = 0;

    for (int i = 0; i < minSamples; i++) {
      short refSample = refBuffer.getShort();
      short ourSample = ourBuffer.getShort();
      long diff = Math.abs((long) refSample - ourSample);

      totalDiff += diff;
      if (diff == 0) {
        matchingSamples++;
      } else if (firstMismatch == -1) {
        firstMismatch = i;
      }
      maxDiff = Math.max(maxDiff, diff);
    }

    double avgDiff = (double) totalDiff / minSamples;
    double matchPercentage = 100.0 * matchingSamples / minSamples;

    System.out.println("\nComparison Results:");
    System.out.println("  Matching samples: " + matchingSamples + "/" + minSamples + " (" + String.format("%.2f%%", matchPercentage) + ")");
    System.out.println("  Average difference: " + String.format("%.2f", avgDiff));
    System.out.println("  Max difference: " + maxDiff);
    System.out.println("  First mismatch at sample: " + firstMismatch);

    // Show samples around first mismatch
    if (firstMismatch >= 0 && firstMismatch < minSamples) {
      System.out.println("\nSamples around first mismatch:");
      int start = Math.max(0, firstMismatch - 3);
      int end = Math.min(minSamples, firstMismatch + 4);

      refBuffer.rewind();
      ourBuffer.rewind();

      for (int i = start; i < end; i++) {
        short refSample = refBuffer.getShort();
        short ourSample = ourBuffer.getShort();
        String marker = (i == firstMismatch) ? " <-- MISMATCH" : "";
        System.out.printf("  Sample %d: ref=%6d, our=%6d, diff=%d%s%n",
            i, refSample, ourSample, Math.abs(refSample - ourSample), marker);
      }
    }
    
    // Also show samples at the beginning where reference has audio
    System.out.println("\nSamples at beginning (0-20):");
    refBuffer.rewind();
    ourBuffer.rewind();
    for (int i = 0; i < 20; i++) {
      short refSample = refBuffer.getShort();
      short ourSample = ourBuffer.getShort();
      if (refSample != 0 || ourSample != 0) {
        System.out.printf("  Sample %d: ref=%6d, our=%6d%n", i, refSample, ourSample);
      }
    }
    
    // Show samples around sample 2712 (where reference has first non-zero)
    System.out.println("\nSamples around 2712 (reference first audio):");
    refBuffer.rewind();
    ourBuffer.rewind();
    for (int i = 0; i < 2712 + 10; i++) {
      refBuffer.getShort();
      ourBuffer.getShort();
    }
    for (int i = 2712; i < 2712 + 10; i++) {
      short refSample = refBuffer.getShort();
      short ourSample = ourBuffer.getShort();
      System.out.printf("  Sample %d: ref=%6d, our=%6d%n", i, refSample, ourSample);
    }

    // Overall assessment
    System.out.println("\n=== Assessment ===");
    if (matchPercentage > 99) {
      System.out.println("EXCELLENT: Outputs match almost perfectly!");
    } else if (matchPercentage > 95) {
      System.out.println("GOOD: Outputs are very similar, minor differences.");
    } else if (matchPercentage > 80) {
      System.out.println("FAIR: Outputs have noticeable differences.");
    } else if (matchPercentage > 50) {
      System.out.println("POOR: Outputs differ significantly.");
    } else {
      System.out.println("FAIL: Outputs are drastically different.");
    }
  }

  /**
   * Save PCM data as a WAV file for manual inspection.
   */
  private void saveToWav(byte[] pcmData, String filename, int sampleRate, int channels, int bitsPerSample) throws IOException {
    int byteRate = sampleRate * channels * bitsPerSample / 8;
    short blockAlign = (short) (channels * bitsPerSample / 8);
    int dataSize = pcmData.length;
    int fileSize = 36 + dataSize;

    ByteBuffer wav = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN);

    // RIFF header
    wav.put("RIFF".getBytes());
    wav.putInt(fileSize);
    wav.put("WAVE".getBytes());

    // fmt sub-chunk
    wav.put("fmt ".getBytes());
    wav.putInt(16); // Sub-chunk size
    wav.putShort((short) 1); // Audio format (PCM)
    wav.putShort((short) channels);
    wav.putInt(sampleRate);
    wav.putInt(byteRate);
    wav.putShort(blockAlign);
    wav.putShort((short) bitsPerSample);

    // data sub-chunk
    wav.put("data".getBytes());
    wav.putInt(dataSize);
    wav.put(pcmData);

    Path outputPath = Paths.get(filename);
    Files.createDirectories(outputPath.getParent());
    Files.write(outputPath, wav.array());
    System.out.println("Saved: " + outputPath.toAbsolutePath());
  }
}
