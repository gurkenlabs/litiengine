package de.gurkenlabs.litiengine.gui;

import java.awt.Graphics2D;
import java.util.EventObject;

public class ComponentRenderEvent extends EventObject {

  private final transient Graphics2D graphics;
  private final transient GuiComponent component;

  /**
   * Constructs a prototypical Event.
   *
   * @param source the object on which the Event initially occurred
   * @throws IllegalArgumentException if source is null
   */
  public ComponentRenderEvent(final Graphics2D graphics, final GuiComponent source) {
    super(source);

    this.graphics = graphics;
    this.component = source;
  }

  /**
   * Get the component involved with the rendering process.
   *
   * @return The component involved with the rendering process.
   */
  public GuiComponent getComponent() {
    return this.component;
  }

  /**
   * Gets the graphics object on which the entity is rendered.
   *
   * @return The graphics object on which the entity is rendered.
   */
  public Graphics2D getGraphics() {
    return this.graphics;
  }
}
