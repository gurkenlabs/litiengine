package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.util.MathUtilities;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * The enum {@code Align} defines a range of horizontal alignments.
 */
@XmlEnum
public enum Align {
  @XmlEnumValue("center")
  CENTER(0.5f),
  @XmlEnumValue("left")
  LEFT(0f),
  @XmlEnumValue("right")
  RIGHT(1f),
  CENTER_LEFT(0.25f),
  CENTER_RIGHT(0.75f);

  private final float portion;

  private Align(float portion) {
    this.portion = portion;
  }

  /**
   * Gets the align enumeration value for the specified string.
   * 
   * @param alignString
   *          The string representing the enum value.
   * @return The enum value represented by the specified string or {@link Align#CENTER} if the specified string is invalid.
   */
  public static Align get(final String alignString) {
    if (alignString == null || alignString.isEmpty()) {
      return Align.CENTER;
    }

    try {
      return Align.valueOf(alignString.toUpperCase());
    } catch (final IllegalArgumentException iae) {
      return Align.CENTER;
    }
  }

  /**
   * Gets the proportional value of this instance.
   * 
   * @param width
   *          The width to calculate the relative value from.
   * @return The proportional value for the specified height.
   */
  public float getValue(float width) {
    return width * this.portion;
  }

  /**
   * Gets the proportional value of this instance.
   * 
   * @param width
   *          The width to calculate the relative value from.
   * @return The proportional value for the specified height.
   */
  public double getValue(double width) {
    return width * this.portion;
  }

  /**
   * Gets the proportional value of this instance.
   * 
   * @param width
   *          The width to calculate the relative value from.
   * @return The proportional value for the specified height.
   */
  public int getValue(int width) {
    return (int) (width * this.portion);
  }

  /**
   * Gets the location for the specified object height to be horizontally aligned within the bounds of the specified width.
   * 
   * @param width
   *          The width, limiting the horizontal alignment.
   * 
   * @param objectWidth
   *          The width of the object for which to calculate the horizontally aligned location.
   * 
   * @return The x-coordinate for the location of the object with the specified width.
   */
  public double getLocation(final double width, final double objectWidth) {
    double value = this.getValue(width);
    double location = value - objectWidth / 2.0;
    if (objectWidth > width) {
      return location;
    }

    return MathUtilities.clamp(location, 0, width - objectWidth);
  }
}
