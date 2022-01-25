package de.gurkenlabs.litiengine.entities;

import java.util.Comparator;

/**
 * This {@code Comparator} implementation sorts entities by the max y-coordinate of their collision box (if it is a
 * {@code ICollisionEntity}) or of their bounding box.
 *
 * @see ICollisionEntity#getCollisionBox()
 * @see IEntity#getBoundingBox()
 * @see Double#compareTo(Double)
 */
public class EntityYComparator implements Comparator<IEntity> {

  @Override
  public int compare(final IEntity m1, final IEntity m2) {
    ICollisionEntity coll1 = null;
    ICollisionEntity coll2 = null;
    if (m1 instanceof ICollisionEntity) {
      coll1 = (ICollisionEntity) m1;
    }

    if (m2 instanceof ICollisionEntity) {
      coll2 = (ICollisionEntity) m2;
    }

    final double m1MaxY =
        coll1 != null ? coll1.getCollisionBox().getMaxY() : m1.getBoundingBox().getMaxY();
    final double m2MaxY =
        coll2 != null ? coll2.getCollisionBox().getMaxY() : m2.getBoundingBox().getMaxY();
    return Double.compare( m1MaxY, m2MaxY );
  }
}
