package de.gurkenlabs.litiengine.util;

import java.awt.Font;

/**
 * Utility class for font-related operations.
 */
public class FontUtilities {

  /**
   * Private constructor to prevent instantiation. Throws UnsupportedOperationException if called.
   */
  private FontUtilities() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns a fallback font if the primary font cannot display the specified string.
   *
   * @param stringToWrite the string to be displayed
   * @param textSize      the size of the text
   * @param primaryFont   the primary font to be used
   * @param fallbackFont  the fallback font to be used if the primary font cannot display the string
   * @return the primary font if it can display the string, otherwise the fallback font
   */
  public static Font getFallbackFontIfNecessary(
    final String stringToWrite,
    final float textSize,
    final Font primaryFont,
    final Font fallbackFont) {
    Font fontToReturn;
    if (primaryFont.canDisplayUpTo(stringToWrite) == -1) {
      fontToReturn = primaryFont.deriveFont(textSize);
    } else {
      fontToReturn = fallbackFont.deriveFont(textSize);
    }
    return fontToReturn;
  }
}
