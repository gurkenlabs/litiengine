package de.gurkenlabs.litiengine.util;

import java.awt.Font;

public class FontUtilities {

  private FontUtilities() {
    throw new IllegalStateException("Utility class");
  }
  
  public static Font getFallbackFontIfNecessary(final String stringToWrite, final float textSize, final Font primaryFont, final Font fallbackFont) {
    Font fontToReturn;
    if (primaryFont.canDisplayUpTo(stringToWrite) == -1) {
      fontToReturn = primaryFont.deriveFont(textSize);
    } else {
      fontToReturn = fallbackFont.deriveFont(textSize);
    }
    return fontToReturn;
  }

}
