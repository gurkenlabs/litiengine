package de.gurkenlabs.litiengine.environment.tilemap;

/**
 * The Enum MapOrientation.
 */
public enum MapOrientation {

  /** The hexagonal. */
  HEXAGONAL,
  /** The isometric. */
  ISOMETRIC,
  /** The orthogonal. */
  ORTHOGONAL,
  /** The shifted. */
  SHIFTED,
  /** The staggered. */
  STAGGERED;

  /**
   * Value.
   *
   * @return the string
   */
  public String value() {
    return this.name().toLowerCase();
  }
}
