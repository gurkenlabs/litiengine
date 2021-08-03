package de.gurkenlabs.litiengine.configuration;

public enum Quality {
  VERYLOW(0), LOW(1), MEDIUM(2), HIGH(3), VERYHIGH(4);

  private final int value;

  private Quality(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
