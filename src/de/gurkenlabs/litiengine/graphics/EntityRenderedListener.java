package de.gurkenlabs.litiengine.graphics;

import java.util.EventListener;

import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * This listener interface is used for receiving events after an entity was rendered with the game's <code>RenderEngine</code>.
 * 
 * @see RenderEngine#renderEntity(java.awt.Graphics2D, IEntity)
 */
public interface EntityRenderedListener extends EventListener {
  /**
   * This method gets called after an entity was rendered.
   * 
   * @param event
   *          The event that contains the render data.
   */
  public void rendered(EntityRenderEvent event);
}
