package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics;

public class RenderEvent<T> {
  private final Graphics graphics;
  private final T rendered;

  public RenderEvent(final Graphics graphics, final T entity) {
    this.graphics = graphics;
    this.rendered = entity;
  }

  public Graphics getGraphics() {
    return this.graphics;
  }

  public T getRenderedObject() {
    return this.rendered;
  }
}
