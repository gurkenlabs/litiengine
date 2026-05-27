package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.environment.Environment;

/**
 * The RenderType defines how and when something is being rendered by the rendering pipeline of the {@code Environment}.
 *
 * @see Environment#render(java.awt.Graphics2D)
 */
public enum RenderType {
  /**
   * Not rendered by the environment pipeline.
   */
  NONE(-1),
  /**
   * Background layer (rendered first).
   */
  BACKGROUND(0),
  /** Ground layer. */
  GROUND(1),
  /** Surface layer above the ground. */
  SURFACE(2),
  /** Default rendering layer for entities. */
  NORMAL(3),
  /** Overlay layer above the default entities. */
  OVERLAY(4),
  /** UI layer (rendered last). */
  UI(5);

  private final int order;

  RenderType(int order) {
    this.order = order;
  }

  /**
   * Gets the integer order used to sort render types within the rendering pipeline.
   *
   * @return the render order
   */
  public int getOrder() {
    return this.order;
  }
}
