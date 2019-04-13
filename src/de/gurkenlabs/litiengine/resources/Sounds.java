package de.gurkenlabs.litiengine.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.UnsupportedAudioFileException;

import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.util.io.Codec;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

public final class Sounds extends ResourcesContainer<Sound> {
  private static final Logger log = Logger.getLogger(Sounds.class.getName());

  Sounds() {
  }

  /**
   * Loads the sound from the specified path and returns it.
   * 
   * @param resourceName
   *          The path of the file to be loaded.(Can be relative or absolute)
   * @return The loaded Sound from the specified path.
   */
  @Override
  protected Sound load(URL resourceName) throws Exception {
    try (final InputStream is = Resources.get(resourceName)) {
      if (is == null) {
        log.log(Level.SEVERE, "The audio file {0} could not be loaded.", new Object[] { resourceName });
        return null;
      }
      return new Sound(is, FileUtilities.getFileName(resourceName));
    }
  }

  public Sound load(final SoundResource resource) {
    byte[] data = Codec.decode(resource.getData());
    ByteArrayInputStream input = new ByteArrayInputStream(data);
    Sound sound;
    try {
      sound = new Sound(input, resource.getName());
      this.add(resource.getName(), sound);
      return sound;
    } catch (IOException | UnsupportedAudioFileException e) {
      log.log(Level.SEVERE, "The audio file {0} could not be loaded.", new Object[] { resource.getName() });
    }

    return null;
  }
}
