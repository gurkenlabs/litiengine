package de.gurkenlabs.litiengine.sound.spi.mp3;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.sound.spi.mp3.Mp3FileReader;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class Mp3FileReaderTests {

  @Test
  void invalidFilesAreRecognized() {
    var fileReader = new Mp3FileReader();
    assertThrows(UnsupportedAudioFileException.class, () -> fileReader.getAudioFileFormat(Resources.getLocation("de/gurkenlabs/litiengine/resources/bop.wav").openStream()));
  }

  @Test
  void testReadFile() throws IOException, UnsupportedAudioFileException {
    var fileReader = new Mp3FileReader();
    var fileFormat = fileReader.getAudioFileFormat(Resources.getLocation("de/gurkenlabs/litiengine/resources/sample.mp3").openStream());

    assertNotNull(fileFormat);

    // Verify the format is correct
    var format = fileFormat.getFormat();
    assertEquals(32000.0f, format.getSampleRate(), 0.01f);
    assertEquals(1, format.getChannels());

    // Test loading the sound through Resources (this triggers the format conversion)
    var sound = Resources.sounds().get("de/gurkenlabs/litiengine/resources/sample.mp3");
    assertNotNull(sound);
    assertNotNull(sound.getFormat());

    // Verify the converted format is PCM
    assertEquals(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, sound.getFormat().getEncoding());
    assertEquals(16, sound.getFormat().getSampleSizeInBits());

    // Verify we have actual audio data
    byte[] data = sound.getStreamData();
    System.out.println("Audio data length: " + data.length);
    assertTrue(data.length > 0, "Audio data should not be empty");

    // Check that data is not all zeros (silence)
    boolean hasNonZero = false;
    int nonZeroCount = 0;
    int zeroCount = 0;
    for (byte b : data) {
      if (b != 0) {
        hasNonZero = true;
        nonZeroCount++;
      } else {
        zeroCount++;
      }
    }
    System.out.println("Non-zero bytes: " + nonZeroCount + " / " + data.length + " (" + zeroCount + " zeros)");
    
    // Print first few values to see what they are
    System.out.println("First 20 bytes: " + java.util.Arrays.toString(java.util.Arrays.copyOf(data, 20)));
    
    // Check the actual sample values (16-bit little-endian)
    // Print first 20 samples
    for (int i = 0; i < Math.min(40, data.length - 1); i += 2) {
      short sample = (short) ((data[i + 1] << 8) | (data[i] & 0xFF));
      System.out.println("Sample " + (i / 2) + ": " + sample);
    }
    
    // Find the first non-zero sample
    int firstNonZero = -1;
    for (int i = 0; i < data.length - 1; i += 2) {
      short sample = (short) ((data[i + 1] << 8) | (data[i] & 0xFF));
      if (sample != 0) {
        firstNonZero = i / 2;
        break;
      }
    }
    System.out.println("First non-zero sample at index: " + firstNonZero);
    
    // Print samples around the first non-zero sample
    if (firstNonZero >= 0) {
      int start = Math.max(0, firstNonZero - 10);
      int end = Math.min(data.length / 2, firstNonZero + 10);
      System.out.println("Samples around first non-zero sample:");
      for (int i = start; i < end; i++) {
        int byteOffset = i * 2;
        short sample = (short) ((data[byteOffset + 1] << 8) | (data[byteOffset] & 0xFF));
        System.out.println("Sample " + i + ": " + sample);
      }
    }
    
    // Print sample distribution (min, max, average of non-zero samples)
    short minSample = Short.MAX_VALUE;
    short maxSample = Short.MIN_VALUE;
    long sum = 0;
    int count = 0;
    int[] histogram = new int[256]; // histogram of 8-bit MSB
    for (int i = 0; i < data.length - 1; i += 2) {
      short sample = (short) ((data[i + 1] << 8) | (data[i] & 0xFF));
      if (sample != 0) {
        minSample = (short) Math.min(minSample, sample);
        maxSample = (short) Math.max(maxSample, sample);
        sum += sample;
        count++;
        // Record histogram of most significant byte
        int msb = (sample >> 8) & 0xFF;
        histogram[msb]++;
      }
    }
    System.out.println("Sample stats: min=" + minSample + ", max=" + maxSample + ", avg=" + (count > 0 ? (double)sum/count : 0) + ", non-zero count=" + count);
    
    // Print histogram summary
    System.out.println("Histogram summary (MSB distribution):");
    for (int i = 0; i < 256; i++) {
      if (histogram[i] > 0) {
        System.out.println("  MSB 0x" + String.format("%02X", i) + ": " + histogram[i] + " samples");
      }
    }
    
    assertTrue(hasNonZero, "Audio data should contain non-zero values");

    // just to play around
    var playback = Game.audio().playSound(sound);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
