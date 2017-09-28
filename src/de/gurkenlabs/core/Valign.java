package de.gurkenlabs.core;

public enum Valign {
  DOWN, MIDDLE, TOP;

  public static Valign get(final String valign) {
    if (valign == null || valign.isEmpty()) {
      return Valign.DOWN;
    }

    try {
      return Valign.valueOf(valign);
    } catch (final IllegalArgumentException iae) {
      return Valign.DOWN;
    }
  }
}