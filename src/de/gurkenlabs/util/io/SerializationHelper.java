package de.gurkenlabs.util.io;

public class SerializationHelper {

  private SerializationHelper() {
  }

  /**
   * Decodes a previously encoded angle.
   *
   * @param encodedAngle
   * @return
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
   * @param precision
   *          The same precision, used for encoding.
   * @return
   */
  public static float decodeSmallFloatingPointNumber(final short encodedNumber, final int precision) {
    return (float) ((encodedNumber + Short.MAX_VALUE) / Math.pow(10, precision));
  }

  /**
   * Encodes an angle, loosing some precision. The encoded / decoded values can
   * differ at max. around 1.43 degrees from the original one.
   *
   * @param angle
   * @return
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
   * * for precision = 1).
   *
   * @param smallNumber
   * @return
   */
  public static short encodeSmallFloatingPointNumber(final float smallNumber, final int precision) {
    if (smallNumber < 0 || (int) (smallNumber * Math.pow(10, precision)) > Short.MAX_VALUE * 2) {
      throw new IllegalArgumentException("The specified number is not within the range to encode.");
    }

    return (short) (smallNumber * Math.pow(10, precision) - Short.MAX_VALUE);
  }
}
