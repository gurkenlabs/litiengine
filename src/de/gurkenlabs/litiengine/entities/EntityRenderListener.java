package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.graphics.RenderEngine;

/**
 * This listener interface is used for receiving events during an entity's rendering process from the game's {@code RenderEngine}.
 * 
 * @see RenderEngine#renderEntity(java.awt.Graphics2D, IEntity)
 */
public interface EntityRenderListener extends EntityRenderedListener {
  /**
   * This method gets called after all rendering checks have successfully passed and right before the entity is about to be rendered.
   * 
   * @param event
   *          The event that contains the render data.
   */
  default void rendering(EntityRenderEvent event) {
  }

  /**
   * This method gets called before an {@code Entity} is about to be rendered.
   * Returning false prevents the rendering of the specified entity.
   * 
   * @param entity
   *          The entity to be rendered.
   * 
   * @return True if the entity should be rendered; otherwise false.
   */
  default boolean canRender(IEntity entity) {
    return true;
  }
}
