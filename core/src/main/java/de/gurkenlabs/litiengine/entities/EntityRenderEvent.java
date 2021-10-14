package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.graphics.RenderEngine;
import java.awt.Graphics2D;
import java.util.EventObject;

/**
 * This {@code EventObject} contains data about the rendering process of an entity.
 *
 * @see RenderEngine#renderEntity(Graphics2D, IEntity)
 */
public class EntityRenderEvent extends EventObject {
  private static final long serialVersionUID = 6397005859146712222L;

  private final transient Graphics2D graphics;
  private final transient IEntity entity;

  public EntityRenderEvent(final Graphics2D graphics, final IEntity entity) {
    super(entity);

    this.graphics = graphics;
    this.entity = entity;
  }

  /**
   * Gets the graphics object on which the entity is rendered.
   *
   * @return The graphics object on which the entity is rendered.
   */
  public Graphics2D getGraphics() {
    return this.graphics;
  }

  /**
   * Get the entity involved with the rendering process.
   *
   * @return The entity involved with the rendering process.
   */
  public IEntity getEntity() {
    return this.entity;
  }
}
