package de.gurkenlabs.litiengine.physics;

import de.gurkenlabs.litiengine.entities.IEntity;
import java.awt.geom.Point2D;

/**
 * The Force implementation sticks to an entity in terms of its location.
 */
public class StickyForce extends Force {

  /**
   * The force entiy.
   */
  private final IEntity forceEntity;

  /**
   * Instantiates a new sticky force.
   *
   * @param forceEntity The entity to which this force will be bound
   * @param strength    The strength/intensity of this force
   * @param size        The size of this force
   */
  public StickyForce(final IEntity forceEntity, final float strength, final float size) {
    super(forceEntity.getCenter(), strength, size);
    this.forceEntity = forceEntity;
  }

  /**
   * Instantiates a new sticky force.
   *
   * @param center   The center point to which this force will be bound
   * @param strength The strength/intensity of this force
   * @param size     The size of this force
   */
  public StickyForce(final Point2D center, final float strength, final float size) {
    super(center, strength, size);
    this.forceEntity = null;
  }

  /**
   * Gets the force entity.
   *
   * @return the force entity
   */
  public IEntity getForceEntity() {
    return this.forceEntity;
  }

  @Override
  public Point2D getLocation() {
    if (this.getForceEntity() != null) {
      return this.getForceEntity().getCenter();
    }
    return super.getLocation();
  }

  @Override
  public boolean cancelOnReached() {
    // always return false because this type of force sticks to the entity by design and can therefore never "reach" the
    // entity
    return false;
  }
}
