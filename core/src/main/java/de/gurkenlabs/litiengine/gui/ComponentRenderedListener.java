package de.gurkenlabs.litiengine.gui;

import java.awt.Graphics2D;
import java.util.EventListener;

/**
 * This listener interface is used for receiving events after an component was rendered.
 *
 * @see GuiComponent#render(Graphics2D)
 */
@FunctionalInterface
public interface ComponentRenderedListener extends EventListener {

  /**
   * This method gets called after an {@link GuiComponent} was rendered.
   *
   * @param event The event that contains the render data.
   */
  void rendered(ComponentRenderEvent event);
}
