package de.gurkenlabs.litiengine.environment.tilemap;

/**
 * The Enum MapOrientation.
 */
public enum MapOrientation {

  /** The hexagonal. */
  hexagonal,
  /** The isometric. */
  isometric,
  /** The orthogonal. */
  orthogonal,
  /** The shifted. */
  shifted,
  /** The staggered. */
  staggered;

  /**
   * Value.
   *
   * @return the string
   */
  public String value() {
    return this.name();
  }
}
