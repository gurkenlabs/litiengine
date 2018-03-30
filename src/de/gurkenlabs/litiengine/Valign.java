package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.util.MathUtilities;

public enum Valign {
  DOWN, MIDDLE, TOP, MIDDLE_TOP, MIDDLE_DOWN;

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

  public double getValue(double height) {
    switch (this) {
    case DOWN:
      return height;
    case MIDDLE:
      return height / 2.0;
    case MIDDLE_DOWN:
      return height * 0.75;
    case MIDDLE_TOP:
      return height * 0.25;
    case TOP:
    default:
      return 0;
    }
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