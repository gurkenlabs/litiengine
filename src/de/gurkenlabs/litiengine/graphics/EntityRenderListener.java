package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.entities.IEntity;

public interface EntityRenderListener extends EntityRenderedListener {
  public default void rendering(EntityRenderEvent event) {
  }

  /**
   * This method gets called before an <code>Entity</code> is about to be rendered.
   * Returning false prevents the rendering of the specified entity.
   * 
   * @param entity
   *          The entity to be rendered.
   * 
   * @return True if the entity should be rendered; otherwise false.
   */
  public default boolean canRender(IEntity entity) {
    return true;
  }
}
