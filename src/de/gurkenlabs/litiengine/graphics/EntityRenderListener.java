package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.entities.IEntity;

public interface EntityRenderListener extends EntityRenderedListener {
  public default void rendering(EntityRenderEvent event) {
  }

  public default boolean canRender(IEntity entity) {
    return true;
  }
}
