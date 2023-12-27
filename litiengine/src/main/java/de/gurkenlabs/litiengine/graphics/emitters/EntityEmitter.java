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

  /**
   * Constructs a new EntityEmitter from a given emitter resource that follows an entity's location.
   *
   * @param entity              The IEntity to which this emitter is bound.
   * @param emitterResourceName The name of the configuration for this emitter. If the Emitter has been added to the game resource file, it is loaded
   *                            from there by name. Otherwise, the EmitterLoader will search the Resource folders for a file with the
   *                            emitterResourceName.
   * @param dynamicLocation     If true, the emitter follows the entity's location; if false, it remains at the location where it has been created.
   */
  public EntityEmitter(
    final IEntity entity, final String emitterResourceName, final boolean dynamicLocation) {
    this(entity, dynamicLocation);
    setEmitterData(emitterResourceName);
  }

  /**
   * Constructs a new EntityEmitter from a given emitter resource that remains at the location where it has been created.
   *
   * @param entity              The IEntity to which this emitter is bound.
   * @param emitterResourceName The name of the configuration for this emitter. If the Emitter has been added to the game resource file, it is loaded
   *                            from there by name. Otherwise, the EmitterLoader will search the Resource folders for a file with the
   *                            emitterResourceName.
   */
  public EntityEmitter(final IEntity entity, final String emitterResourceName) {
    this(entity, emitterResourceName, false);
  }

  /**
   * Constructs a new EntityEmitter with EmitterData that follows an entity's location.
   *
   * @param entity          The IEntity to which this emitter is bound.
   * @param emitterData     The EmitterData object defining the emitter's behavior.
   * @param dynamicLocation If true, the emitter follows the entity's location; if false, it remains at the location where it has been created.
   */
  public EntityEmitter(
    final IEntity entity, final EmitterData emitterData, final boolean dynamicLocation) {
    this(entity, dynamicLocation);
    setEmitterData(emitterData);
  }

  /**
   * Constructs a new EntityEmitter with EmitterData that remains at the location where it has been created.
   *
   * @param entity      The IEntity to which this emitter is bound.
   * @param emitterData The EmitterData object defining the emitter's behavior.
   */
  public EntityEmitter(final IEntity entity, final EmitterData emitterData) {
    this(entity, emitterData, false);
  }

  /**
   * Instantiates a new entity emitter that remains at the location where it has been created.
   *
   * @param entity The IEntity to which this emitter is bound.
   */
  public EntityEmitter(final IEntity entity) {
    this(entity, false);
  }

  /**
   * Instantiates a new entity emitter.
   *
   * @param entity          The IEntity to which this emitter is bound.
   * @param dynamicLocation If true, the emitter follows the entity's location; if false, it remains at the location where it has been created.
   */
  public EntityEmitter(final IEntity entity, boolean dynamicLocation) {
    super();
    this.entity = entity;
    this.dynamicLocation = dynamicLocation;
    setX(entity.getCenter().getX() - getWidth() / 2d);
    setY(entity.getCenter().getY() - getHeight() / 2d);
  }

  /**
   * Get the IEntity to which this emitter is bound.
   *
   * @return The IEntity to which this emitter is bound.
   */
  public IEntity getEntity() {
    return this.entity;
  }

  /**
   * Check if the emitter has dynamic location tracking enabled. This determines whether this Emitter's location is updated along its entity's
   * location.
   *
   * @return True if dynamic location is enabled, false otherwise.
   */
  public boolean hasDynamicLocation() {
    return this.dynamicLocation;
  }

  /**
   * Toggle dynamic location updates.
   *
   * @param dynamicLocation If true, the emitter follows its entity's location; if false, it remains at the location where it has been created.
   */
  public void setDynamicLocation(boolean dynamicLocation) {
    this.dynamicLocation = dynamicLocation;
  }

  @Override
  public double getWidth() {
    return getEntity() != null ? getEntity().getWidth() : super.getWidth();
  }

  @Override
  public double getHeight() {
    return getEntity() != null ? getEntity().getHeight() : super.getHeight();
  }

  @Override
  public double getX() {
    return getEntity() != null && hasDynamicLocation() ? getEntity().getX() : super.getX();
  }

  @Override
  public double getY() {
    return getEntity() != null && hasDynamicLocation() ? getEntity().getY() : super.getY();
  }

  @Override
  public Point2D getLocation() {
    return getEntity() != null && hasDynamicLocation() ? getEntity().getLocation()
      : super.getLocation();
  }
}
