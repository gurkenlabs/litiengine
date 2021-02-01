package com.litiengine.entities;

public enum Rotation {
  NONE, ROTATE_90, ROTATE_180, ROTATE_270;

  public double getRadians() {
    int value;
    switch (this) {
    case ROTATE_90:
      value = 90;
      break;
    case ROTATE_180:
      value = 180;
      break;
    case ROTATE_270:
      value = 270;
      break;
    default:
      value = 0;
      break;
    }

    return Math.toRadians(value);
  }
}
