package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;

/**
 * A functional interface which indicates that implementing instances can render some visual content onto a provided
 * graphics context.
 *
 * @see Graphics2D
 */
@FunctionalInterface
public interface IRenderable {
  /**
   * Renders the visual contents of this instance onto the provided graphics context.
   *
   * <p>
   * If an {@code Entity} implements this interface, this method will be called right after the entity was rendered from
   * the environment. Allowing for a custom rendering mechanism.
   *
   * <p>
   * This interface can be implemented in general by anything that should be rendered to the game's screen.
   *
   * @param g
   *          The current graphics object onto which this instance will render its visual contents.
   * @see RenderEngine#renderEntity(Graphics2D, de.gurkenlabs.litiengine.entities.IEntity)
   */
  void render(Graphics2D g);
}
