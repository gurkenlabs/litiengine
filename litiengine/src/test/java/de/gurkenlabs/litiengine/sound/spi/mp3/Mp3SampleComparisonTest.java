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

class Mp3SampleComparisonTest {

  private static final String MP3_RESOURCE = "de/gurkenlabs/litiengine/resources/sample.mp3";

  @Test
  void investigateGranuleBoundary() throws Exception {
    InputStream mp3Stream = Resources.getLocation(MP3_RESOURCE).openStream();
    byte[] mp3Data = mp3Stream.readAllBytes();
    mp3Stream.close();

    byte[] ourPcm = decodeWithOurImplementation(mp3Data);
    if (ourPcm.length < 200) {
        return;
    }
    ByteBuffer buf = ByteBuffer.wrap(ourPcm).order(ByteOrder.LITTLE_ENDIAN);
    System.out.println("First 100 samples:");
    for (int i = 0; i < 100; i++) {
      short sample = buf.getShort();
      if (sample != 0) {
        System.out.printf("Sample %d: %d\n", i, sample);
      }
    }
    
    // Show samples around granule boundaries
    System.out.println("\nSamples around 576 (granule 0->1 boundary):");
    buf.position(570 * 2);
    for (int i = 570; i < 590; i++) {
      System.out.printf("Sample %d: %d\n", i, buf.getShort());
    }
    
    System.out.println("\nSamples around 1152 (granule 1->2 boundary):");
    buf.position(1146 * 2);
    for (int i = 1146; i < 1160; i++) {
      System.out.printf("Sample %d: %d\n", i, buf.getShort());
    }
    
    // Count garbage per granule
    buf.rewind();
    int[] garbagePerGranule = new int[20];
    for (int i = 0; i < ourPcm.length / 2; i++) {
      short sample = buf.getShort();
      int granule = i / 576;
      if (granule < 20 && (sample <= -32760 || sample >= 32760)) {
        garbagePerGranule[granule]++;
      }
    }
    
    System.out.println("\nGarbage per granule:");
    for (int g = 0; g < 20; g++) {
      if (garbagePerGranule[g] > 0) {
        System.out.printf("Granule %d: %d garbage samples\n", g, garbagePerGranule[g]);
      }
    }
  }

  private byte[] decodeWithMp3Spi(byte[] mp3Data) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data);
    AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(bais);
    AudioFormat baseFormat = mp3Stream.getFormat();
    AudioFormat decodedFormat = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
        baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
    AudioInputStream pcmStream = AudioSystem.getAudioInputStream(decodedFormat, mp3Stream);
    byte[] pcmData = pcmStream.readAllBytes();
    pcmStream.close(); mp3Stream.close();
    return pcmData;
  }

  private byte[] decodeWithOurImplementation(byte[] mp3Data) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data);
    Mp3FileReader reader = new Mp3FileReader();
    AudioInputStream mp3Stream = reader.getAudioInputStream(bais);
    AudioFormat baseFormat = mp3Stream.getFormat();
    AudioFormat decodedFormat = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
        baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
    AudioInputStream pcmStream = AudioSystem.getAudioInputStream(decodedFormat, mp3Stream);
    byte[] pcmData = pcmStream.readAllBytes();
    pcmStream.close(); mp3Stream.close();
    return pcmData;
  }
}
