package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.graphics.RenderType;
import java.awt.Graphics2D;
import java.util.EventListener;

/**
 * This listener provides call backs for different points during rendering an {@code Environment}.
 *
 * @see Environment#render(Graphics2D)
 */
@FunctionalInterface
public interface EnvironmentRenderedListener extends EventListener {
  /**
   * This method is called after the {@code Environment} rendered everything of the specified {@code
   * RenderType}.
   *
   * @param g The graphics object that is being rendered to.
   * @param type The render type for which all instances were just rendered.
   */
  void rendered(Graphics2D g, RenderType type);
}
