package de.gurkenlabs.litiengine.entities;

import java.util.Comparator;

public class EntityYComparator implements Comparator<IEntity> {

  @Override
  public int compare(final IEntity m1, final IEntity m2) {
    if (m1 instanceof IMovableCombatEntity && m2 instanceof IMovableCombatEntity) {
      if (((IMovableCombatEntity) m1).isDead() && !((IMovableCombatEntity) m2).isDead()) {
        return -1;
      }

      if (!((IMovableCombatEntity) m1).isDead() && ((IMovableCombatEntity) m2).isDead()) {
        return 1;
      }
    }

    ICollisionEntity coll1 = null;
    ICollisionEntity coll2 = null;
    if (m1 instanceof ICollisionEntity) {
      coll1 = (ICollisionEntity) m1;
    }

    if (m2 instanceof ICollisionEntity) {
      coll2 = (ICollisionEntity) m2;
    }

    final double m1MaxY = coll1 != null ? coll1.getCollisionBox().getMaxY() : m1.getBoundingBox().getMaxY();
    final double m2MaxY = coll2 != null ? coll2.getCollisionBox().getMaxY() : m2.getBoundingBox().getMaxY();
    return Double.valueOf(m1MaxY).compareTo(m2MaxY);
  }
}
