package de.gurkenlabs.litiengine;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import de.gurkenlabs.litiengine.util.MathUtilities;

/**
 * The enum <code>Valign</code> defines a range of vertical alignments.
 */
@XmlEnum
public enum Valign {
  @XmlEnumValue("bottom")
  DOWN(1f),
  @XmlEnumValue("center")
  MIDDLE(0.5f),
  @XmlEnumValue("top")
  TOP(0f),
  MIDDLE_TOP(0.25f),
  MIDDLE_DOWN(0.75f);

  private final float portion;

  private Valign(float portion) {
    this.portion = portion;
  }

  public static Valign get(final String valign) {
    if (valign == null || valign.isEmpty()) {
      return Valign.DOWN;
    }

    try {
      return Valign.valueOf(valign.toUpperCase());
    } catch (final IllegalArgumentException iae) {
      return Valign.DOWN;
    }
  }

  /**
   * Gets the proportional value of this instance.
   * 
   * @param height
   *          The height to calculate the relative value from.
   * @return The proportional value for the specified height.
   */
  public float getValue(float height) {
    return height * this.portion;
  }

  /**
   * Gets the proportional value of this instance.
   * 
   * @param height
   *          The height to calculate the relative value from.
   * @return The proportional value for the specified height.
   */
  public double getValue(double height) {
    return height * this.portion;
  }

  /**
   * Gets the proportional value of this instance.
   * 
   * @param height
   *          The height to calculate the relative value from.
   * @return The proportional value for the specified height.
   */
  public int getValue(int height) {
    return (int) (height * this.portion);
  }

  public double getLocation(final double height, final double objectHeight) {
    double value = this.getValue(height);
    double location = value - objectHeight / 2.0;

    if (objectHeight > height) {
      return location;
    }

    return MathUtilities.clamp(location, 0, height - objectHeight);
  }
}