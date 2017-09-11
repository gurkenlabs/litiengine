package de.gurkenlabs.litiengine.gui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.util.io.FileUtilities;

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
   * Gets the font.
   *
   * @param fontName
   *          the font name
   * @return the font
   */
  public static Font load(final String fontName) {
    if (fonts.containsKey(fontName)) {
      return fonts.get(fontName);
    }

    final String[] f = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    for (final String font : f) {
      final String name = FileUtilities.getFileName(fontName);
      if (font.equals(name)) {
        return new Font(name, Font.PLAIN, 8);
      }
    }
    try {
      final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

      final InputStream fontStream = FileUtilities.getGameResource(fontName);
      if (fontStream == null) {
        log.log(Level.SEVERE, "font '%s' could not be loaded", fontName);
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
