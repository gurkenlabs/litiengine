package de.gurkenlabs.litiengine.sound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.gurkenlabs.util.io.FileUtilities;
import de.gurkenlabs.util.io.StreamUtilities;

public class Sound {
  private static final List<Sound> sounds = new CopyOnWriteArrayList<>();

  public static Sound find(final String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    final Optional<Sound> sound = sounds.stream().filter(x -> x.getName().equalsIgnoreCase(FileUtilities.getFileName(name))).findFirst();
    if (!sound.isPresent()) {
      return null;
    }

    return sound.get();
  }

  private final String name;

  private final String path;

  private byte[] streamData;

  private AudioFormat format;

  private AudioInputStream stream;

  public Sound(final String path) {
    this.name = FileUtilities.getFileName(path);
    this.path = path;

    InputStream is = FileUtilities.getGameResource(path);

    try {
      this.stream = AudioSystem.getAudioInputStream(is);
      if (this.stream != null) {
        this.streamData = StreamUtilities.getBytes(this.stream);
      }
      this.format = stream.getFormat();
    } catch (UnsupportedAudioFileException e) {
      System.out.println("could not load '" + path + "'");
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    sounds.add(this);
  }

  public String getName() {
    return this.name;
  }

  public String getPath() {
    return this.path;
  }

  public URL getUrl() {
    try {
      final File file = new File(this.getPath());
      if (file.exists()) {
        return file.toURI().toURL();
      }

      return this.getClass().getClassLoader().getResource(this.getPath());
    } catch (final MalformedURLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public AudioInputStream getStream() {
    if (this.format == null) {
      return null;
    }

    return new AudioInputStream(new ByteArrayInputStream(this.streamData), this.getFormat(), this.streamData.length);
  }

  public AudioFormat getFormat() {
    return this.format;
  }
}
