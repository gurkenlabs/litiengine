package de.gurkenlabs.litiengine.entities;

public enum Direction {
  DOWN((byte) 1), LEFT((byte) 2), RIGHT((byte) 4), UNDEFINED((byte) 8), UP((byte) 16);

  public static Direction fromAngle(final float angle) {
    if (angle >= 0 && angle < 45) {
      return Direction.DOWN;
    }
    if (angle >= 45 && angle < 135) {
      return Direction.RIGHT;
    }
    if (angle >= 135 && angle < 225) {
      return Direction.UP;
    }
    if (angle >= 225 && angle < 315) {
      return Direction.LEFT;
    }

    if (angle >= 315 && angle <= 360) {
      return Direction.DOWN;
    }

    return Direction.UNDEFINED;
  }

  public static Direction fromFlagValue(final byte flagValue) {
    for (final Direction dir : Direction.values()) {
      if (dir.getFlagValue() == flagValue) {
        return dir;
      }
    }

    return UNDEFINED;
  }

  public static float toAngle(final Direction dir) {
    switch (dir) {

    case RIGHT:
      return 90;
    case UP:
      return 360;
    case LEFT:
      return 270;
    case DOWN:
      return 180;
    default:
      return 0;
    }
  }

  private final byte flagValue;

  private Direction(final byte flagValue) {
    this.flagValue = flagValue;
  }

  public byte getFlagValue() {
    return this.flagValue;
  }

}
