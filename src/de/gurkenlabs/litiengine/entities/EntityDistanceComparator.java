package de.gurkenlabs.litiengine.entities;

import java.util.Comparator;

public class EntityDistanceComparator implements Comparator<IEntity> {
  private final IEntity relativeEntity;

  public EntityDistanceComparator(final IEntity relativeEntity) {
    this.relativeEntity = relativeEntity;
  }

  @Override
  public int compare(final IEntity entity1, final IEntity entity2) {
    final double distance1 = entity1.getLocation().distance(this.relativeEntity.getLocation());
    final double distance2 = entity2.getLocation().distance(this.relativeEntity.getLocation());
    if (distance1 < distance2) {
      return -1;
    }
    if (distance1 > distance2) {
      return 1;
    }

    return 0;
  }
}
