package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.resources.Resources;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class Mp3ComparisonDebugTest {

  @Test
  void compareWithReference() throws Exception {
    var mp3Url = Resources.getLocation("de/gurkenlabs/litiengine/resources/sample.mp3");
    byte[] mp3Data;
    try (var is = mp3Url.openStream()) {
      mp3Data = is.readAllBytes();
    }

    // Decode with reference (mp3spi)
    byte[] refData = decodeWithMp3Spi(mp3Data);
    System.out.println("Reference: " + refData.length + " bytes (" + (refData.length / 2) + " samples)");

    // Decode with our implementation
    byte[] ourData = decodeWithLitiengine(mp3Data);
    System.out.println("LITIENGINE: " + ourData.length + " bytes (" + (ourData.length / 2) + " samples)");

    // Find first non-zero in each
    ByteBuffer refBuf = ByteBuffer.wrap(refData).order(ByteOrder.LITTLE_ENDIAN);
    int refFirstNonZero = -1;
    for (int i = 0; i < refData.length / 2; i++) {
      if (refBuf.getShort() != 0) {
        refFirstNonZero = i;
        break;
      }
    }

    ByteBuffer ourBuf = ByteBuffer.wrap(ourData).order(ByteOrder.LITTLE_ENDIAN);
    int ourFirstNonZero = -1;
    for (int i = 0; i < ourData.length / 2; i++) {
      if (ourBuf.getShort() != 0) {
        ourFirstNonZero = i;
        break;
      }
    }

    System.out.println("\n=== First Non-Zero Sample ===");
    System.out.println("Reference: " + refFirstNonZero + " (" + String.format("%.3f", refFirstNonZero / 32000.0) + " seconds)");
    System.out.println("LITIENGINE: " + ourFirstNonZero + " (" + String.format("%.3f", ourFirstNonZero / 32000.0) + " seconds)");
    System.out.println("Difference: " + (ourFirstNonZero - refFirstNonZero) + " samples");

    // Show samples around first non-zero
    if (refFirstNonZero >= 0) {
      System.out.println("\n=== Reference samples " + refFirstNonZero + " to " + (refFirstNonZero + 10) + " ===");
      refBuf.position(refFirstNonZero * 2);
      for (int i = 0; i < 10; i++) {
        System.out.println("  " + (refFirstNonZero + i) + ": " + refBuf.getShort());
      }
    }

    if (ourFirstNonZero >= 0) {
      System.out.println("\n=== LITIENGINE samples " + ourFirstNonZero + " to " + (ourFirstNonZero + 10) + " ===");
      ourBuf.position(ourFirstNonZero * 2);
      for (int i = 0; i < 10; i++) {
        System.out.println("  " + (ourFirstNonZero + i) + ": " + ourBuf.getShort());
      }
    }

    // Compare first 100 samples after first non-zero
    System.out.println("\n=== Comparing first 100 samples after first non-zero ===");
    refBuf.position(refFirstNonZero * 2);
    ourBuf.position(ourFirstNonZero * 2);
    
    int diffCount = 0;
    int maxDiff = 0;
    for (int i = 0; i < 100; i++) {
      short ref = refBuf.getShort();
      short ours = ourBuf.getShort();
      if (ref != ours) {
        diffCount++;
        int diff = Math.abs(ref - ours);
        maxDiff = Math.max(maxDiff, diff);
        if (diffCount <= 5) {
          System.out.println("  Sample " + i + ": ref=" + ref + ", ours=" + ours + ", diff=" + diff);
        }
      }
    }
    System.out.println("Differences: " + diffCount + " / 100");
    System.out.println("Max diff: " + maxDiff);
  }

  private byte[] decodeWithMp3Spi(byte[] mp3Data) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data);
    AudioInputStream ais = AudioSystem.getAudioInputStream(bais);
    AudioFormat base = ais.getFormat();
    AudioFormat decoded = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        base.getSampleRate(),
        16,
        base.getChannels(),
        base.getChannels() * 2,
        base.getSampleRate(),
        false
    );
    AudioInputStream pcm = AudioSystem.getAudioInputStream(decoded, ais);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int r;
    while ((r = pcm.read(buf)) > 0) baos.write(buf, 0, r);
    ais.close();
    pcm.close();
    return baos.toByteArray();
  }

  private byte[] decodeWithLitiengine(byte[] mp3Data) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data);
    Mp3FileReader reader = new Mp3FileReader();
    AudioInputStream ais = reader.getAudioInputStream(bais);
    AudioFormat base = ais.getFormat();
    AudioFormat decoded = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        base.getSampleRate(),
        16,
        base.getChannels(),
        base.getChannels() * 2,
        base.getSampleRate(),
        false
    );
    AudioInputStream pcm = AudioSystem.getAudioInputStream(decoded, ais);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int r;
    while ((r = pcm.read(buf)) > 0) baos.write(buf, 0, r);
    ais.close();
    pcm.close();
    return baos.toByteArray();
  }
}
