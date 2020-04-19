package de.gurkenlabs.litiengine.graphics.emitters;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * An abstract implementation for emitters that are bound to <code>IEntity.getLocation()</code>.
 * 
 * @see IEntity#getLocation()
 */
public abstract class EntityEmitter extends Emitter {

  private final IEntity entity;

  /**
   * Instantiates a new entity emitter.
   *
   * @param entity
   *          the entity
   */
  public EntityEmitter(final IEntity entity) {
    super(entity.getX(), entity.getY());
    this.entity = entity;
    this.setSize(this.getEntity().getWidth(), this.getEntity().getHeight());
  }

  public IEntity getEntity() {
    return this.entity;
  }

  @Override
  public Point2D getLocation() {
    if (this.getEntity() == null) {
      return null;
    }
    return this.getEntity().getLocation();
  }

}
