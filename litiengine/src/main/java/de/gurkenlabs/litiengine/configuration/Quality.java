package de.gurkenlabs.litiengine.configuration;

/**
 * Enum representing different quality levels.
 */
public enum Quality {
  VERYLOW(0),
  LOW(1),
  MEDIUM(2),
  HIGH(3),
  VERYHIGH(4);

  private final int value;

  /**
   * Constructs a Quality enum with the specified value.
   *
   * @param value The integer value representing the quality level.
   */
  private Quality(int value) {
    this.value = value;
  }

  /**
   * Gets the integer value of the quality level.
   *
   * @return The integer value of the quality level.
   */
  public int getValue() {
    return value;
  }
}
