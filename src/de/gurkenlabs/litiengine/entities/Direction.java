package de.gurkenlabs.litiengine.entities;

public enum Direction {
  DOWN((byte) 1),
  LEFT((byte) 2),
  RIGHT((byte) 4),
  UNDEFINED((byte) 8),
  UP((byte) 16);

  private final byte flagValue;

  private Direction(final byte flagValue) {
    this.flagValue = flagValue;
  }

  public byte getFlagValue() {
    return this.flagValue;
  }

  public static Direction fromFlagValue(byte flagValue) {
    for (Direction dir : Direction.values()) {
      if (dir.getFlagValue() == flagValue) {
        return dir;
      }
    }

    return UNDEFINED;
  }
}
