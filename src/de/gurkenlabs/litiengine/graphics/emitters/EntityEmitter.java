package de.gurkenlabs.litiengine.graphics.emitters;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.IEntityProvider;
import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * An abstract implementation for emitters that are bound to <code>IEntity.getLocation()</code>.
 * 
 * @see IEntity#getLocation()
 */
public abstract class EntityEmitter extends Emitter implements IEntityProvider {

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
