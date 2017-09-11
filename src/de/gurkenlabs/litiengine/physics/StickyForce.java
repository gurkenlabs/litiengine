package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * The Force implementation sticks to an entity in terms of its location.
 */
public class StickyForce extends Force {

  /** The force entiy. */
  private final IEntity forceEntiy;

  /**
   * Instantiates a new sticky force.
   *
   */
  public StickyForce(final IEntity forceEntity, final float strength, final float size) {
    super(forceEntity.getDimensionCenter(), strength, size);
    this.forceEntiy = forceEntity;
  }

  public StickyForce(final Point2D center, final float strength, final float size) {
    super(center, strength, size);
    this.forceEntiy = null;
  }

  /**
   * Gets the force entiy.
   *
   * @return the force entiy
   */
  public IEntity getForceEntiy() {
    return this.forceEntiy;
  }

  /*
   * (non-Javadoc)
   *
   */
  @Override
  public Point2D getLocation() {
    if (this.getForceEntiy() != null) {
      return this.getForceEntiy().getDimensionCenter();
    }
    return super.getLocation();
  }
}
