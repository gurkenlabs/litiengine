package de.gurkenlabs.litiengine.environment.tilemap;

public enum StaggerIndex {
  ODD,
  EVEN,
  UNDEFINED;

  public String value() {
    return this.name().toLowerCase();
  }
}
