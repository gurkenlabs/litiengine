package de.gurkenlabs.litiengine.environment.tilemap;

public enum MapOrientation {
  UNDEFINED,
  HEXAGONAL,
  ISOMETRIC,
  ORTHOGONAL,
  SHIFTED,
  STAGGERED;

  public String value() {
    return this.name().toLowerCase();
  }
}
