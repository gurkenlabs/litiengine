package de.gurkenlabs.litiengine.util.io;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import de.gurkenlabs.litiengine.graphics.ImageFormat;

public final class Codec {
  private static final Logger log = Logger.getLogger(Codec.class.getName());

  private Codec() {
    throw new UnsupportedOperationException();
  }

  /**
   * Decodes a previously encoded angle.
   *
   * @param encodedAngle
   *          The encoded angle.
   * @return The decoded angle.
   */
  public static float decodeAngle(final byte encodedAngle) {
    float angle = encodedAngle;
    angle += 127;
    angle /= 256 / 360.0f;
    angle += 360;
    return angle % 360;
  }

  public static float decodeAngleFromShort(final short encodedAngle) {
    return decodeSmallFloatingPointNumber(encodedAngle, 2);
  }

  /**
   * Decodes a small floating point number, previously encoded with
   * {@link #encodeSmallFloatingPointNumber(float, int)
   * encodeSmallFloatingPointNumber}.
   *
   * @param encodedNumber
   *          The encoded number
   * @param precision
   *          The precision of the encoded number. The same precision, used for
   *          encoding.
   * @return The decoded small floating point number.
   */
  public static float decodeSmallFloatingPointNumber(final short encodedNumber, final int precision) {
    return (float) ((encodedNumber + Short.MAX_VALUE) / Math.pow(10, precision));
  }

  /**
   * Encodes an angle, loosing some precision. The encoded / decoded values can
   * differ at max. around 1.43 degrees from the original one.
   *
   * @param angle
   *          The angle
   * @return The encoded angle.
   */
  public static byte encodeAngle(final float angle) {
    float encodedAngle = angle % 360;
    encodedAngle *= 256 / 360.0f;
    encodedAngle -= 127;

    return (byte) encodedAngle;
  }

  public static short encodeAngleToShort(final float angle) {
    float encodedAngle = angle;
    if (encodedAngle < 0) {
      encodedAngle += 360;
    }

    encodedAngle %= 360;

    return encodeSmallFloatingPointNumber(encodedAngle, 2);
  }

  /**
   * Encodes positive numbers less than Short.MAX_VALUE * 2 / precision (6553.4
   * for precision = 1).
   *
   * @param smallNumber
   *          The small number to encode
   * @param precision
   *          The comma precision for the encoding process.
   * @return The encoded number.
   */
  public static short encodeSmallFloatingPointNumber(final float smallNumber, final int precision) {
    if (smallNumber < 0 || (int) (smallNumber * Math.pow(10, precision)) > Short.MAX_VALUE * 2) {
      throw new IllegalArgumentException("The specified number is not within the range to encode.");
    }

    return (short) (smallNumber * Math.pow(10, precision) - Short.MAX_VALUE);
  }

  public static BufferedImage decodeImage(final String imageString) {
    if (imageString == null) {
      return null;
    }

    BufferedImage image = null;
    byte[] imageByte;
    try {
      imageByte = decode(imageString);
      final ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
      image = ImageIO.read(bis);
      bis.close();
    } catch (final Exception e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    return image;
  }

  public static String encode(final BufferedImage image) {
    return encode(image, ImageFormat.PNG);
  }

  public static String encode(final BufferedImage image, ImageFormat imageFormat) {
    if (image == null) {
      return null;
    }

    String imageString = null;
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    try {
      ImageIO.write(image, imageFormat != ImageFormat.UNDEFINED ? imageFormat.toString() : ImageFormat.PNG.toString(), bos);
      final byte[] imageBytes = bos.toByteArray();

      imageString = encode(imageBytes);

      bos.close();
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    return imageString;
  }

  public static String encode(byte[] data) {
    return Base64.getEncoder().encodeToString(data);
  }

  public static byte[] decode(String base64) {
    return Base64.getDecoder().decode(base64);
  }
}
