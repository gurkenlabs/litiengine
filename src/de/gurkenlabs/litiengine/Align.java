package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.util.MathUtilities;

public enum Align {
  CENTER, LEFT, RIGHT, CENTER_LEFT, CENTER_RIGHT;

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

  public double getValue(double width) {
    switch (this) {
    case RIGHT:
      return width;
    case CENTER:
      return width / 2.0;
    case CENTER_RIGHT:
      return width * 0.75;
    case CENTER_LEFT:
      return width * 0.25;
    case LEFT:
    default:
      return 0;
    }
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