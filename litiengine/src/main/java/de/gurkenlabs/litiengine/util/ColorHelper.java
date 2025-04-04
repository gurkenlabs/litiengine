package de.gurkenlabs.litiengine.util;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for color-related helper methods. This class cannot be instantiated.
 */
public final class ColorHelper {

  private static final Logger log = Logger.getLogger(ColorHelper.class.getName());
  private static final int HEX_STRING_LENGTH = 7;
  private static final int HEX_STRING_LENGTH_ALPHA = 9;
  private static final int MAX_RGB_VALUE = 255;

  private ColorHelper() {
    throw new UnsupportedOperationException();
  }

  /**
   * Encodes the specified color to a hexadecimal string representation. The output format is:
   *
   * <ul>
   * <li>#RRGGBB - For colors without alpha
   * <li>#AARRGGBB - For colors with alpha
   * </ul>
   * <p>
   * Examples: <br>
   * {@code Color.RED} = "#ff0000"<br>
   * {@code new Color(255, 0, 0, 200)} = "#c8ff0000"
   *
   * @param color The color that is encoded.
   * @return An hexadecimal string representation of the specified color.
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
   * Decodes the specified color string to an actual {@code Color} instance. The accepted format is:
   * <p>
   * <i>Note: This returns null if the format of the provided color string is invalid.</i>
   * </p>
   *
   * <ul>
   * <li>#RRGGBB - For colors without alpha
   * <li>#AARRGGBB - For colors with alpha
   * </ul>
   * <p>
   * Examples: <br>
   * "#ff0000" = {@code Color.RED}<br>
   * "#c8ff0000" = {@code new Color(255, 0, 0, 200)}
   *
   * @param colorHexString The hexadecimal encodes color string representation.
   * @return The decoded color.
   * @see ColorHelper#encode(Color)
   * @see Color
   * @see Color#decode(String)
   * @see Integer#decode(String)
   */
  public static Color decode(String colorHexString) {
    return decode(colorHexString, false);
  }

  /**
   * Decodes the specified color string to an actual {@code Color} instance, with an option to create a darker version of the base color. The accepted
   * format is:
   * <ul>
   * <li>#RRGGBB - For colors without alpha
   * <li>#AARRGGBB - For colors with alpha
   * </ul>
   * <p>
   * If the `solid` parameter is true, the color alpha will create a darker version of the base color.
   * </p>
   * <p>
   * Examples: <br>
   * "#ff0000" = {@code Color.RED}<br>
   * "#c8ff0000" = {@code new Color(255, 0, 0, 200)}
   * </p>
   *
   * @param colorHexString The hexadecimal encoded color string representation.
   * @param solid          If true, the color alpha will create a darker version of the base color.
   * @return The decoded {@code Color} object, or {@code null} if the input string is invalid.
   * @see ColorHelper#encode(Color)
   * @see Color
   * @see Color#decode(String)
   * @see Integer#decode(String)
   */
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

    return switch (colorHexString.length()) {
      case HEX_STRING_LENGTH -> decodeWellformedHexString(colorHexString);
      case HEX_STRING_LENGTH_ALPHA -> decodeHexStringWithAlpha(colorHexString, solid);
      default -> {
        log.log(Level.SEVERE,
          "Could not parse color string \"{0}\". Invalid string length \"{1}\"!\nAccepted lengths:\n\t{2} for Colors without Alpha (#ff0000)\n\t{3} for Colors with Alpha (#c8ff0000)",
          new Object[] {colorHexString, colorHexString.length(), HEX_STRING_LENGTH, HEX_STRING_LENGTH_ALPHA});
        yield null;
      }
    };
  }

  /**
   * Ensures that the specified value lies within the accepted range for Color values (0-255). Smaller values will be forced to be 0 and larger values
   * will result in 255.
   *
   * @param value The value to check for.
   * @return An integer value that fits the color value restrictions.
   */
  public static int ensureColorValueRange(float value) {
    return ensureColorValueRange(Math.round(value));
  }

  /**
   * Ensures that the specified value lies within the accepted range for Color values (0-255). Smaller values will be forced to be 0 and larger values
   * will result in 255.
   *
   * @param value The value to check for.
   * @return An integer value that fits the color value restrictions.
   */
  public static int ensureColorValueRange(int value) {
    return Math.clamp(value, 0, MAX_RGB_VALUE);
  }

  /**
   * Premultiplies the alpha on the given color.
   *
   * @param color The color to premultiply
   * @return The color given, with alpha replaced with a black background.
   */
  public static Color premultiply(Color color) {
    if (color.getAlpha() == 255) {
      return color;
    }
    return new Color(premultiply(color.getRed(), color.getAlpha()), premultiply(color.getGreen(), color.getAlpha()),
      premultiply(color.getBlue(), color.getAlpha()));
  }

  /**
   * Interpolates between two colors based on the given factor. The factor determines the weight of each color in the interpolation. A factor of 0.0
   * will return the first color, and a factor of 1.0 will return the second color. Intermediate values will return a blend of the two colors.
   *
   * @param color1 The first color.
   * @param color2 The second color.
   * @param factor The interpolation factor, ranging from 0.0 to 1.0.
   * @return A new {@code Color} object that is the result of interpolating between the two colors.
   */
  public static Color interpolate(Color color1, Color color2, double factor) {
    factor = Math.clamp(factor, 0, 1);

    int r = (int) (color1.getRed() * (1 - factor) + color2.getRed() * factor);
    int g = (int) (color1.getGreen() * (1 - factor) + color2.getGreen() * factor);
    int b = (int) (color1.getBlue() * (1 - factor) + color2.getBlue() * factor);
    int a = (int) (color1.getAlpha() * (1 - factor) + color2.getAlpha() * factor);

    return new Color(r, g, b, a);
  }

  /**
   * Returns a transparent variant of the specified color with the given alpha value. The red, green, and blue components of the color remain
   * unchanged.
   *
   * @param color    The original color.
   * @param newAlpha The new alpha value to be applied to the color.
   * @return A new {@code Color} object with the specified alpha value.
   */
  public static Color getTransparentVariant(Color color, int newAlpha) {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), ensureColorValueRange(newAlpha));
  }

  /**
   * Decodes a well-formed hexadecimal color string to a {@code Color} object. This method expects the input string to be in the format:
   * <ul>
   * <li>#RRGGBB - For colors without alpha
   * </ul>
   * <p>
   * If the input string is not well-formed, this method logs a {@code SEVERE} level message and returns {@code null}.
   * </p>
   *
   * @param hexString The well-formed hexadecimal color string representation.
   * @return The decoded {@code Color} object, or {@code null} if the input string is invalid.
   */
  private static Color decodeWellformedHexString(String hexString) {
    try {
      return Color.decode(hexString);
    } catch (NumberFormatException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    return null;
  }

  /**
   * Premultiplies the given color value with the specified alpha value. This method accounts for gamma correction.
   *
   * @param value The color value to be premultiplied.
   * @param alpha The alpha value to premultiply with.
   * @return The premultiplied color value.
   */
  private static int premultiply(int value, int alpha) {
    // account for gamma
    return (int) Math.round(value * Math.pow(alpha / 255.0, 1 / 2.2));
  }

  /**
   * Decodes a hexadecimal color string with an alpha component to a {@code Color} object. The accepted format is:
   * <ul>
   * <li>#AARRGGBB - For colors with alpha
   * </ul>
   * <p>
   * If the `solid` parameter is true, the color alpha will create a darker version of the base color.
   * </p>
   * <p>
   * Examples: <br>
   * "#c8ff0000" = {@code new Color(255, 0, 0, 200)}
   * </p>
   *
   * @param hexString The hexadecimal encoded color string representation with alpha.
   * @param solid     If true, the color alpha will create a darker version of the base color.
   * @return The decoded {@code Color} object, or null if the input string is invalid.
   */
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
    baseColor = new Color(baseColor.getRGB() & 0xffffff | alphaValue << 24, true);
    // solid means that color alpha will basically create a darker version of the base color
    if (solid) {
      return premultiply(baseColor);
    } else {
      return baseColor;
    }
  }
}
