package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IEntityProvider;

/**
 * An abstract implementation for emitters that are bound to
 * {@link de.gurkenlabs.litiengine.entities.IEntity#getLocation()}.
 */
public abstract class EntityEmitter extends Emitter implements IEntityProvider {

  /** The entity. */
  private final IEntity entity;

  /**
   * Instantiates a new entity emitter.
   *
   * @param entity
   *          the entity
   */
  public EntityEmitter(final IEntity entity) {
    super(entity.getCenter().getX() - entity.getWidth() / 2, entity.getCenter().getY() - entity.getHeight() / 2);
    this.entity = entity;
    this.setSize(this.getEntity().getWidth(), this.getEntity().getHeight());
  }

  @Override
  public IEntity getEntity() {
    return this.entity;
  }

  @Override
  public Point2D getLocation() {
    if (this.getEntity() == null) {
      return null;
    }

    return super.getLocation();
  }

}
