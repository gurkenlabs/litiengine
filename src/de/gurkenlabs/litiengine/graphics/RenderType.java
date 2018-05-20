package de.gurkenlabs.litiengine.graphics;

public enum RenderType {
  NONE(-1), BACKGROUND(0), GROUND(1), SURFACE(2), NORMAL(3), OVERLAY(4), UI(5);

  private final int order;

  private RenderType(int order) {
    this.order = order;
  }

  public int getOrder() {
    return this.order;
  }
}
