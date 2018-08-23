package de.gurkenlabs.litiengine.environment.tilemap;

public enum StaggerAxis {
  X,
  Y,
  UNDEFINED;

  public String value() {
    return this.name().toLowerCase();
  }
}
