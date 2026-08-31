package de.gurkenlabs.litiengine;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

/// The enum `Valign` defines a range of vertical alignments.
@XmlEnum
public enum Valign {
  @XmlEnumValue("DOWN")
  DOWN(1f),
  @XmlEnumValue("MIDDLE")
  MIDDLE(0.5f),
  @XmlEnumValue("TOP")
  TOP(0f),
  @XmlEnumValue("MIDDLE_TOP")
  MIDDLE_TOP(0.25f),
  @XmlEnumValue("MIDDLE_DOWN")
  MIDDLE_DOWN(0.75f);

  private final float portion;

  Valign(float portion) {
    this.portion = portion;
  }

  /// Gets the vertical align enumeration value for the specified string.
  ///
  /// @param valignString The string representing the enum value.
  /// @return The enum value represented by the specified string or [Valign#DOWN] if the specified string is invalid.
  public static Valign get(final String valignString) {
    if (valignString == null || valignString.isEmpty()) {
      return Valign.DOWN;
    }

    try {
      return Valign.valueOf(valignString.toUpperCase());
    } catch (final IllegalArgumentException iae) {
      return Valign.DOWN;
    }
  }

  /// Gets the proportional value of this instance.
  ///
  /// @param height The height to calculate the relative value from.
  /// @return The proportional value for the specified height.
  public float getValue(float height) {
    return height * this.portion;
  }

  /// Gets the proportional value of this instance.
  ///
  /// @param height The height to calculate the relative value from.
  /// @return The proportional value for the specified height.
  public double getValue(double height) {
    return height * this.portion;
  }

  /// Gets the proportional value of this instance.
  ///
  /// @param height The height to calculate the relative value from.
  /// @return The proportional value for the specified height.
  public int getValue(int height) {
    return (int) (height * this.portion);
  }

  /// Gets the location for the specified object height to be vertically aligned relatively to the bounds of the specified height.
  /// Suitable for
  /// **entity** alignment. The return value might be negative or exceed the bottom boundary which is
  /// *`height` - `objectHeight`*.
  ///
  /// For **text** alignment [double, boolean)][#getLocation(double,] should be used with
  /// *`preventOverflow`* set to `true`.
  ///
  /// @param height       The height, limiting the vertical alignment.
  /// @param objectHeight The height of the object for which to calculate the vertically aligned location.
  /// @return The y-coordinate for the location of the object with the specified height.
  public double getLocation(final double height, final double objectHeight) {
    return getLocation(height, objectHeight, false);
  }

  /// Gets the location for the specified object height to be vertically aligned relatively to the bounds of the specified height. An overflow behavior
  /// (whenever *`objectHeight` > `height`*) can be controlled using
  /// **`preventOverflow`** parameter:
  ///
  /// - *false*: the return value might be negative or exceed the bottom boundary which is *`height` -
  /// `objectHeight`* (good for **entity** alignment).
  /// - *true*: the return value will always be clamped within the bounds (good for **text** alignment).
  ///
  /// @param height          The height, limiting the vertical alignment.
  /// @param objectHeight    The height of the object for which to calculate the vertically aligned location.
  /// @param preventOverflow A flag indicating whether the return value should be clamped to keep it within the bounds (prevent values that are
  /// negative or beyond the bottom boundary).
  /// @return The y-coordinate for the location of the object with the specified height.
  public double getLocation(final double height, final double objectHeight, final boolean preventOverflow) {
    double value = this.getValue(height);
    double location = value - objectHeight / 2.0;

    if (objectHeight > height && !preventOverflow) {
      return location;
    }

    return Math.max(0, Math.min(height - objectHeight, location));
  }

  /// Gets the portion value of the alignment.
  ///
  /// @return The portion value of the alignment.
  public float getPortion() {
    return portion;
  }
}
