package de.gurkenlabs.litiengine.resources;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Fonts extends ResourcesContainer<Font> {
  private static final Logger log = Logger.getLogger(Fonts.class.getName());

  Fonts() {
  }

  public Font get(String name, float size) {
    Font font = this.get(name);
    if (font == null) {
      return null;
    }

    return font.deriveFont(size);
  }

  public Font get(String name, int style) {
    Font font = this.get(name);
    if (font == null) {
      return null;
    }

    return font.deriveFont(style);
  }

  public Font get(String name, int style, float size) {
    Font font = this.get(name);
    if (font == null) {
      return null;
    }

    return font.deriveFont(style, size);
  }

  /***
   * Loads a custom font with the specified name from game's resources.
   * As a fallback, when no font could be found by the specified <code>fontName</code>, it tries to get the font from the environment by calling.
   *
   * @param resourceName
   *          The name of the font
   * @return The loaded font.
   * 
   * @see Font#createFont(int, java.io.File)
   * @see Font#getFont(String)
   */
  @Override
  protected Font load(String resourceName) {
    try {
      final InputStream fontStream = Resources.get(resourceName);
      if (fontStream == null) {
        log.log(Level.SEVERE, "font {0} could not be loaded", resourceName);
        return null;
      }

      return Font.createFont(Font.TRUETYPE_FONT, fontStream);
    } catch (final FontFormatException | IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return Font.getFont(resourceName);
  }
}
