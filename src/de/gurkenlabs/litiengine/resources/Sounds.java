package de.gurkenlabs.litiengine.resources;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.sound.Sound;

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
  protected Sound load(String resourceName) throws Exception {
    final InputStream is = Resources.get(resourceName);
    if (is == null) {

      log.log(Level.SEVERE, "The audio file {0} could not be loaded.", new Object[] { resourceName });
      return null;
    }
    return new Sound(is, resourceName);
  }
}
