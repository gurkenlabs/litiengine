package de.gurkenlabs.util.io;

public class SerializationHelper {

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
    final float smallNumber = (float) ((encodedNumber + Short.MAX_VALUE) / Math.pow(10, precision));
    return smallNumber;
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

    final short number = (short) (smallNumber * Math.pow(10, precision) - Short.MAX_VALUE);
    return number;
  }
}
