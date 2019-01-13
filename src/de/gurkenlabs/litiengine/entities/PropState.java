package de.gurkenlabs.litiengine.entities;

public enum PropState {
  INTACT,
  DAMAGED,
  DESTROYED;

  public String spriteString() {
    return this.name().toLowerCase();
  }
}
