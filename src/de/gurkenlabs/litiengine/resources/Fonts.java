package de.gurkenlabs.litiengine.resources;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.util.io.FileUtilities;

public final class Fonts extends ResourcesContainer<Font> {
  private static final Logger log = Logger.getLogger(Fonts.class.getName());

  Fonts() {
  }

  /***
   * Loads a custom font with the specified name from game's resources and registers it on the <code>GraphicsEnvironment</code>.
   * As a fallback, when no font could be found by the specified <code>fontName</code>, it tries to get the font from the environment by calling.
   *
   * @param resourceName
   *          The name of the font
   * @return The loaded font.
   * 
   * @see GraphicsEnvironment#registerFont(Font)
   * @see Font#createFont(int, java.io.File)
   * @see Font#getFont(String)
   */
  @Override
  protected Font load(String resourceName) {
    try {
      final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

      final InputStream fontStream = FileUtilities.getGameResource(resourceName);
      if (fontStream == null) {
        log.log(Level.SEVERE, "font {0} could not be loaded", resourceName);
        return null;
      }

      final Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
      ge.registerFont(font);
      return font;
    } catch (final FontFormatException | IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return Font.getFont(resourceName);
  }
}
