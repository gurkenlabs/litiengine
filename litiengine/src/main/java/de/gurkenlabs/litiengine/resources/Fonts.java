package de.gurkenlabs.litiengine.resources;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A container class for managing font resources. This class extends the ResourcesContainer class, specifically for Font objects.
 */
public final class Fonts extends ResourcesContainer<Font> {
  private static final Logger log = Logger.getLogger(Fonts.class.getName());

  Fonts() {
  }

  /**
   * Retrieves a font with the specified name and size.
   *
   * @param name The name of the font.
   * @param size The size of the font.
   * @return The derived font with the specified size, or null if the font is not found.
   */
  public Font get(String name, float size) {
    Font font = this.get(name);
    if (font == null) {
      return null;
    }

    return font.deriveFont(size);
  }

  /**
   * Retrieves a font with the specified name and style.
   *
   * @param name  The name of the font.
   * @param style The style of the font (e.g., Font.PLAIN, Font.BOLD).
   * @return The derived font with the specified style, or null if the font is not found.
   */
  public Font get(String name, int style) {
    Font font = this.get(name);
    if (font == null) {
      return null;
    }

    return font.deriveFont(style);
  }

  /**
   * Retrieves a font with the specified name, style, and size.
   *
   * @param name  The name of the font.
   * @param style The style of the font (e.g., Font.PLAIN, Font.BOLD).
   * @param size  The size of the font.
   * @return The derived font with the specified style and size, or null if the font is not found.
   */
  public Font get(String name, int style, float size) {
    Font font = this.get(name);
    if (font == null) {
      return null;
    }

    return font.deriveFont(style, size);
  }

  /***
   * Loads a custom font with the specified name from game's resources. As a fallback, when no font could be found by the
   * specified {@code fontName}, it tries to get the font from the environment by calling.
   *
   * @param resourceName
   *          The name of the font
   * @return The loaded font.
   *
   * @see Font#createFont(int, java.io.File)
   * @see Font#getFont(String)
   */
  @Override
  protected Font load(URL resourceName) {
    try (final InputStream fontStream = Resources.get(resourceName)) {
      if (fontStream == null) {
        log.log(Level.SEVERE, "font {0} could not be loaded", resourceName);
        return null;
      }

      return Font.createFont(Font.TRUETYPE_FONT, fontStream);
    } catch (final FontFormatException | IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }
}
