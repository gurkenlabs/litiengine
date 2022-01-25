package de.gurkenlabs.litiengine.entities;

public enum Rotation {
  NONE,
  ROTATE_90,
  ROTATE_180,
  ROTATE_270;

  public double getRadians() {
    int value = switch ( this ) {
      case ROTATE_90 -> 90;
      case ROTATE_180 -> 180;
      case ROTATE_270 -> 270;
      default -> 0;
    };

    return Math.toRadians(value);
  }
}
