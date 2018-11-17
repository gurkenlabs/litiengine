package de.gurkenlabs.litiengine.util;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ColorHelper {
  private static final Logger log = Logger.getLogger(ColorHelper.class.getName());
  private static final int HEX_STRING_LENGTH = 7;
  private static final int HEX_STRING_LENGTH_ALPHA = 9;
  private static final int MAX_RGB_VALUE = 255;

  private ColorHelper() {
  }

  /**
   * Encodes the specified color to a hexadecimal string representation.
   * The output format is:
   * <ul>
   * <li>#RRGGBB - For colors without alpha</li>
   * <li>#AARRGGBB - For colors with alpha</li>
   * </ul>
   * Examples: <br>
   * <code>Color.RED</code> = "#ff0000"<br>
   * <code>new Color(255, 0, 0, 200)</code> = "#c8ff0000"
   * 
   * @param color
   *          The color that is encoded.
   * @return An hexadecimal string representation of the specified color.
   * 
   * @see ColorHelper#decode(String)
   * @see Color
   * @see Color#getRGB()
   * @see Integer#toHexString(int)
   */
  public static String encode(Color color) {
    if (color == null) {
      return null;
    }

    String colorString = String.format("%08x", color.getRGB());
    if (color.getAlpha() == MAX_RGB_VALUE) {
      return "#" + colorString.substring(2);
    } else {
      return "#" + colorString;
    }
  }

  /**
   * Decodes the specified color string to an actual <code>Color</code> instance.
   * The accepted format is:
   * <ul>
   * <li>#RRGGBB - For colors without alpha</li>
   * <li>#AARRGGBB - For colors with alpha</li>
   * </ul>
   * Examples: <br>
   * "#ff0000" = <code>Color.RED</code><br>
   * "#c8ff0000" = <code>new Color(255, 0, 0, 200)</code>
   * 
   * @param colorHexString
   *          The hexadecimal encodes color string representation.
   * @return The decoded color.
   * 
   * @see ColorHelper#encode(Color)
   * @see Color
   * @see Color#decode(String)
   * @see Integer#decode(String)
   */
  public static Color decode(String colorHexString) {
    return decode(colorHexString, false);
  }

  public static Color decode(String colorHexString, boolean solid) {
    if (colorHexString == null || colorHexString.isEmpty()) {
      return null;
    }

    if (!colorHexString.startsWith("#")) {
      if (colorHexString.length() == HEX_STRING_LENGTH - 1 || colorHexString.length() == HEX_STRING_LENGTH_ALPHA - 1) {
        colorHexString = "#" + colorHexString;
      } else {
        log.log(Level.SEVERE, "Could not parse color string \"{0}\". A color string needs to start with a \"#\" character.", colorHexString);
        return null;
      }
    }

    switch (colorHexString.length()) {
    case HEX_STRING_LENGTH:
      return decodeWellformedHexString(colorHexString);
    case HEX_STRING_LENGTH_ALPHA:
      return decodeHexStringWithAlpha(colorHexString, solid);
    default:
      log.log(Level.SEVERE, "Could not parse color string \"{0}\". Invalid string length \"{1}\"!\nAccepted lengths:\n\t{2} for Colors without Alpha (#ff0000)\n\t{3} for Colors with Alpha (#c8ff0000)",
          new Object[] { colorHexString, colorHexString.length(), HEX_STRING_LENGTH, HEX_STRING_LENGTH_ALPHA });
      return null;
    }
  }

  /**
   * Ensures that the specified value lies within the accepted range for Color values (0-255).
   * Smaller values will be forced to be 0 and larger values will result in 255.
   * 
   * @param value
   *          The value to check for.
   * @return An integer value that fits the color value restrictions.
   */
  public static int ensureColorValueRange(float value) {
    return ensureColorValueRange(Math.round(value));
  }

  /**
   * Ensures that the specified value lies within the accepted range for Color values (0-255).
   * Smaller values will be forced to be 0 and larger values will result in 255.
   * 
   * @param value
   *          The value to check for.
   * @return An integer value that fits the color value restrictions.
   */
  public static int ensureColorValueRange(int value) {
    return Math.min(Math.max(value, 0), MAX_RGB_VALUE);
  }

  private static Color decodeWellformedHexString(String hexString) {
    try {
      return Color.decode(hexString);
    } catch (NumberFormatException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    return null;
  }

  private static Color decodeHexStringWithAlpha(String hexString, boolean solid) {
    String alpha = hexString.substring(1, 3);

    int alphaValue;
    try {
      alphaValue = ensureColorValueRange(Integer.parseInt(alpha, 16));
    } catch (NumberFormatException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }

    StringBuilder sb = new StringBuilder(hexString);
    sb.replace(1, 3, "");
    String baseColorString = sb.toString();
    Color baseColor = decodeWellformedHexString(baseColorString);
    if (baseColor == null) {
      return null;
    }
    // solid means that color alpha will basically create a darker version of the base color
    if (solid) {
      float alphaRatio = alphaValue / (float) MAX_RGB_VALUE;
      int red = ensureColorValueRange(alphaRatio * baseColor.getRed());
      int green = ensureColorValueRange(alphaRatio * baseColor.getGreen());
      int blue = ensureColorValueRange(alphaRatio * baseColor.getBlue());
      return new Color(red, green, blue);
    } else {
      return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alphaValue);
    }
  }
}
