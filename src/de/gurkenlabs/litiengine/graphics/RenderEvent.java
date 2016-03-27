package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;

public class RenderEvent<T> {
  private final Graphics2D graphics;
  private final T rendered;

  public RenderEvent(final Graphics2D graphics, final T entity) {
    this.graphics = graphics;
    this.rendered = entity;
  }

  public Graphics2D getGraphics() {
    return this.graphics;
  }

  public T getRenderedObject() {
    return this.rendered;
  }
}
