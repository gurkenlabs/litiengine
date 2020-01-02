package de.gurkenlabs.litiengine;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import de.gurkenlabs.litiengine.util.MathUtilities;

/**
 * The enum <code>Align</code> defines a range of horizontal alignments.
 */
@XmlEnum
public enum Align {
  @XmlEnumValue("center")
  CENTER(0.5f),
  @XmlEnumValue("left")
  LEFT(0f),
  @XmlEnumValue("right")
  RIGHT(1f),
  @XmlEnumValue("justify")
  JUSTIFY(Float.NaN), // for use in TMX maps
  CENTER_LEFT(0.25f), 
  CENTER_RIGHT(0.75f);

  public final float portion;

  private Align(float portion) {
    this.portion = portion;
  }

  public static Align get(final String align) {
    if (align == null || align.isEmpty()) {
      return Align.CENTER;
    }

    try {
      return Align.valueOf(align.toUpperCase());
    } catch (final IllegalArgumentException iae) {
      return Align.CENTER;
    }
  }

  public float getValue(float width) {
    return width * this.portion;
  }

  public double getValue(double width) {
    return width * this.portion;
  }

  public double getLocation(final double width, final double objectWidth) {
    double value = this.getValue(width);
    double location = value - objectWidth / 2.0;
    if (objectWidth > width) {
      return location;
    }

    return MathUtilities.clamp(location, 0, width - objectWidth);
  }
}