package de.gurkenlabs.litiengine.entities;

public enum PropState {
  INTACT,
  DAMAGED,
  DESTROYED;

  private final String str;

  PropState() {
    this.str = this.name().toLowerCase();
  }

  public String spriteString() {
    return this.str;
  }
}
