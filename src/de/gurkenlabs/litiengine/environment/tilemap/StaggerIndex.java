package de.gurkenlabs.litiengine.environment.tilemap;

public enum StaggerIndex {
  ODD,
  EVEN;

  public String value() {
    return this.name().toLowerCase();
  }
}
