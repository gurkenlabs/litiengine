package de.gurkenlabs.litiengine.graphics.emitters;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import java.awt.geom.Point2D;

/**
 * A standard implementation for emitters that are bound to {@code IEntity.getLocation()}.
 *
 * @see IEntity#getLocation()
 */
public class EntityEmitter extends Emitter {

  private final IEntity entity;
  private boolean dynamicLocation;

  public EntityEmitter(
      final IEntity entity, final String emitterXml, final boolean dynamicLocation) {
    this(entity, dynamicLocation);
    setEmitterData(emitterXml);
  }

  public EntityEmitter(final IEntity entity, final String emitterXml) {
    this(entity, emitterXml, false);
  }

  public EntityEmitter(
      final IEntity entity, final EmitterData emitterData, final boolean dynamicLocation) {
    this(entity, dynamicLocation);
    setEmitterData(emitterData);
  }

  public EntityEmitter(final IEntity entity, final EmitterData emitterData) {
    this(entity, emitterData, false);
  }

  /**
   * Instantiates a new entity emitter.
   *
   * @param entity the entity
   */
  public EntityEmitter(final IEntity entity) {
    this(entity, false);
  }

  /**
   * Instantiates a new entity emitter.
   *
   * @param entity the entity
   * @param dynamicLocation if true, move the Emitter along with its Entity once it moves. If false,
   *     always keep the original Location of the Emitter
   */
  public EntityEmitter(final IEntity entity, boolean dynamicLocation) {
    super(entity.getX(), entity.getY());
    this.entity = entity;
    this.setSize(this.getEntity().getWidth(), this.getEntity().getHeight());
    this.dynamicLocation = dynamicLocation;
  }

  public IEntity getEntity() {
    return this.entity;
  }

  public boolean hasDynamicLocation() {
    return this.dynamicLocation;
  }

  @Override
  public Point2D getLocation() {
    if (this.getEntity() == null) {
      return null;
    }
    return this.hasDynamicLocation() ? this.getEntity().getLocation() : super.getLocation();
  }
}
