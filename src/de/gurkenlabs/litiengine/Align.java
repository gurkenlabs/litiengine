package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.util.MathUtilities;

public enum Align {
  CENTER(0.5f), LEFT(0f), RIGHT(1f), CENTER_LEFT(0.25f), CENTER_RIGHT(0.75f);

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