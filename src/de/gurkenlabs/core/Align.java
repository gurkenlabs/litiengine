package de.gurkenlabs.core;

public enum Align {
  CENTER, LEFT, RIGHT;

  public static Align get(final String align) {
    if (align == null || align.isEmpty()) {
      return Align.CENTER;
    }

    try {
      return Align.valueOf(align);
    } catch (final IllegalArgumentException iae) {
      return Align.CENTER;
    }
  }
}