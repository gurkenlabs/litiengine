package de.gurkenlabs.litiengine.gui;

import java.awt.Graphics2D;

/**
 * This listener interface is used for receiving events during a component's rendering process.
 *
 * @see GuiComponent#render(Graphics2D)
 */
public interface ComponentRenderListener extends ComponentRenderedListener {

  /**
   * This method gets called after all rendering checks have successfully passed and right before the component is about
   * to be rendered.
   *
   * @param event
   *          The event that contains the render data.
   */
  default void rendering(ComponentRenderEvent event) {}

  /**
   * This method gets called before an {@code GuiComponent} is about to be rendered. Returning false prevents the
   * rendering of the specified component.
   *
   * @param component
   *          The component to be rendered.
   * @return True if the component should be rendered; otherwise false.
   */
  default boolean canRender(GuiComponent component) {
    return true;
  }
}
