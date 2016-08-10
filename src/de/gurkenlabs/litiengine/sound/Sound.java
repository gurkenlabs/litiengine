package de.gurkenlabs.litiengine.sound;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class Sound {
  private static final List<Sound> sounds = new CopyOnWriteArrayList<>();

  public static Sound find(final String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    final Optional<Sound> sound = sounds.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findFirst();
    if (!sound.isPresent()) {
      return null;
    }

    return sound.get();
  }

  private final String name;

  private final String path;

  public Sound(final String path) {
    this.name = path;
    this.path = path;
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
}
