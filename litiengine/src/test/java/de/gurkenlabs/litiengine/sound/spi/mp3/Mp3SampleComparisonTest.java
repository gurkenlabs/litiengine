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

/**
 * Compare the first 1000 samples between reference and our implementation.
 */
class Mp3SampleComparisonTest {

  private static final String MP3_RESOURCE = "de/gurkenlabs/litiengine/resources/sample.mp3";

  @Test
  void compareFirst1000Samples() throws Exception {
    System.out.println("=== Sample Comparison Test ===\n");

    InputStream mp3Stream = Resources.getLocation(MP3_RESOURCE).openStream();
    byte[] mp3Data = mp3Stream.readAllBytes();
    mp3Stream.close();

    // Decode with reference
    byte[] referencePcm = decodeWithMp3Spi(mp3Data);
    System.out.println("Reference: " + referencePcm.length + " bytes (" + (referencePcm.length / 2) + " samples)");

    // Decode with our implementation
    byte[] ourPcm = decodeWithOurImplementation(mp3Data);
    System.out.println("Our impl:  " + ourPcm.length + " bytes (" + (ourPcm.length / 2) + " samples)");

    // Compare first 1000 samples
    ByteBuffer refBuf = ByteBuffer.wrap(referencePcm).order(ByteOrder.LITTLE_ENDIAN);
    ByteBuffer ourBuf = ByteBuffer.wrap(ourPcm).order(ByteOrder.LITTLE_ENDIAN);

    int numSamples = Math.min(1000, Math.min(referencePcm.length, ourPcm.length) / 2);
    System.out.println("\nComparing first " + numSamples + " samples:\n");

    int matchCount = 0;
    int totalDiff = 0;
    int maxDiff = 0;
    int firstMismatch = -1;

    System.out.println("Sample | Reference | Ours   | Diff");
    System.out.println("-------|-----------|--------|-----");

    for (int i = 0; i < numSamples; i++) {
      short refSample = refBuf.getShort();
      short ourSample = ourBuf.getShort();
      int diff = Math.abs(refSample - ourSample);

      if (diff == 0) {
        matchCount++;
      } else if (firstMismatch == -1) {
        firstMismatch = i;
      }

      totalDiff += diff;
      maxDiff = Math.max(maxDiff, diff);

      // Print first 20 samples and any mismatches in first 100
      if (i < 20 || (i < 100 && diff > 0)) {
        System.out.printf("%6d | %9d | %6d | %5d%s\n", i, refSample, ourSample, diff, diff > 0 ? " <--" : "");
      }
    }

    System.out.println("\n=== Statistics ===");
    System.out.println("Match rate: " + matchCount + "/" + numSamples + " (" + String.format("%.1f%%", 100.0 * matchCount / numSamples) + ")");
    System.out.println("Average diff: " + String.format("%.2f", (double) totalDiff / numSamples));
    System.out.println("Max diff: " + maxDiff);
    System.out.println("First mismatch at: " + firstMismatch);

    // Find where reference has first non-zero
    refBuf.rewind();
    int refFirstNonZero = -1;
    for (int i = 0; i < referencePcm.length / 2; i++) {
      if (refBuf.getShort() != 0) {
        refFirstNonZero = i;
        break;
      }
    }

    // Find where we have first non-zero
    ourBuf.rewind();
    int ourFirstNonZero = -1;
    for (int i = 0; i < ourPcm.length / 2; i++) {
      if (ourBuf.getShort() != 0) {
        ourFirstNonZero = i;
        break;
      }
    }

    System.out.println("\nFirst non-zero sample:");
    System.out.println("  Reference: " + refFirstNonZero);
    System.out.println("  Ours: " + ourFirstNonZero);
    if (refFirstNonZero >= 0 && ourFirstNonZero >= 0) {
      System.out.println("  Difference: " + (ourFirstNonZero - refFirstNonZero) + " samples");
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

  private byte[] decodeWithOurImplementation(byte[] mp3Data) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data);
    Mp3FileReader reader = new Mp3FileReader();
    AudioInputStream mp3Stream = reader.getAudioInputStream(bais);

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
}
