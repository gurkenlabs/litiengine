package de.gurkenlabs.litiengine.environment.tilemap;

public enum StaggerAxis {
  X,
  Y;

  public String value() {
    return this.name().toLowerCase();
  }
}
