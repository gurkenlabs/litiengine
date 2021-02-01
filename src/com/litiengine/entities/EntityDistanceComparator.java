package com.litiengine.entities;

public class EntityDistanceComparator extends RelativeEntityComparator {

  /**
   * Initializes a new instance of the {@code EntityDistanceComparator} class.
   *
   * @param relativeEntity
   *          The entity that is used as reference for distance comparison.
   */
  public EntityDistanceComparator(final IEntity relativeEntity) {
    super(relativeEntity);
  }

  @Override
  public int compare(final IEntity entity1, final IEntity entity2) {
    if (this.getRelativeEntity() == null) {
      return 0;
    }

    final double distance1 = entity1.getLocation().distance(this.getRelativeEntity().getLocation());
    final double distance2 = entity2.getLocation().distance(this.getRelativeEntity().getLocation());
    return Double.compare(distance1,distance2);
  }
}
