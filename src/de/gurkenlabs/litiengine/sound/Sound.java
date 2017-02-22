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

  private AudioFormat format;

  private final String name;

  private AudioInputStream stream;

  private byte[] streamData;

  private Sound(final String path) {
    this.name = FileUtilities.getFileName(path);

    final InputStream is = FileUtilities.getGameResource(path);

    try {
      AudioInputStream in = AudioSystem.getAudioInputStream(is);
      if (in != null) {
        final AudioFormat baseFormat = in.getFormat();
        final AudioFormat decodedFormat = this.getOutFormat(baseFormat);
        // Get AudioInputStream that will be decoded by underlying VorbisSPI
        in = AudioSystem.getAudioInputStream(decodedFormat, in);
        this.stream = in;
        this.streamData = StreamUtilities.getBytes(this.stream);
      }
      this.format = this.stream.getFormat();
    } catch (final UnsupportedAudioFileException e) {
      System.out.println("could not load '" + path + "'");
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public AudioFormat getFormat() {
    return this.format;
  }

  public String getName() {
    return this.name;
  }

  public byte[] getStreamData() {
    if (this.streamData == null) {
      return new byte[0];
    }

    final byte[] data = this.streamData.clone();
    return data;
  }

  private AudioFormat getOutFormat(final AudioFormat inFormat) {
    final int ch = inFormat.getChannels();
    final float rate = inFormat.getSampleRate();
    return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
  }
}