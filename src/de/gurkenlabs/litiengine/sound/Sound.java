package de.gurkenlabs.litiengine.sound;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.gurkenlabs.util.io.FileUtilities;
import de.gurkenlabs.util.io.StreamUtilities;

public class Sound {
  private static final Map<String, Sound> sounds = new ConcurrentHashMap<>();
  private final String name;

  private byte[] streamData;

  private AudioFormat format;

  private AudioInputStream stream;

  private Sound(final String path) {
    this.name = FileUtilities.getFileName(path);

    InputStream is = FileUtilities.getGameResource(path);

    try {
      AudioInputStream in = AudioSystem.getAudioInputStream(is);
      if (in != null) {
        AudioFormat baseFormat = in.getFormat();
        AudioFormat decodedFormat = this.getOutFormat(baseFormat);
        // Get AudioInputStream that will be decoded by underlying VorbisSPI
        in = AudioSystem.getAudioInputStream(decodedFormat, in);
        this.stream = in;
        this.streamData = StreamUtilities.getBytes(this.stream);
      }
      this.format = this.stream.getFormat();
    } catch (UnsupportedAudioFileException e) {
      System.out.println("could not load '" + path + "'");
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Sound find(final String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    return sounds.get(FileUtilities.getFileName(name));
  }

  public static Sound load(final String path) {
    Sound sound = sounds.get(FileUtilities.getFileName(path));
    if (sound != null) {
      return sound;
    }

    sound = new Sound(path);
    sounds.put(FileUtilities.getFileName(path), sound);
    return sound;
  }

  public String getName() {
    return this.name;
  }

  public AudioFormat getFormat() {
    return this.format;
  }

  private AudioFormat getOutFormat(AudioFormat inFormat) {
    final int ch = inFormat.getChannels();
    final float rate = inFormat.getSampleRate();
    return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
  }

  public byte[] getStreamData() {
    if (this.streamData == null) {
      return new byte[0];
    }

    byte[] data = this.streamData.clone();
    return data;
  }
}