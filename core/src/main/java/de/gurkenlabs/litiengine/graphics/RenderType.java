package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.environment.Environment;

/**
 * The RenderType defines how and when something is being rendered by the rendering pipeline of the {@code Environment}.
 *
 * @see Environment#render(java.awt.Graphics2D)
 */
public enum RenderType {
  NONE(-1),
  BACKGROUND(0),
  GROUND(1),
  SURFACE(2),
  NORMAL(3),
  OVERLAY(4),
  UI(5);

  private final int order;

  private RenderType(int order) {
    this.order = order;
  }

  public int getOrder() {
    return this.order;
  }
}
