package de.gurkenlabs.litiengine.gui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.util.io.FileUtilities;

/**
 * The Class FontLoader loads custom fonts to the java environment.
 */
public class FontLoader {
  /** The Constant fonts. */
  private static final HashMap<String, Font> fonts = new HashMap<>();
  private static final Logger log = Logger.getLogger(FontLoader.class.getName());

  private FontLoader() {
  }

  /**
   * Loads a custom font with the specified name from game's resources.
   *
   * @param fontName
   *          The name of the font
   * @return The loaded font.
   */
  public static Font load(final String fontName) {
    if (fonts.containsKey(fontName)) {
      return fonts.get(fontName);
    }

    try {
      final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

      final InputStream fontStream = FileUtilities.getGameResource(fontName);
      if (fontStream == null) {
        log.log(Level.SEVERE, "font {0} could not be loaded", fontName);
        return null;
      }

      final Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
      ge.registerFont(font);
      fonts.put(fontName, font);
      return font;
    } catch (final FontFormatException | IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return null;
  }
}
