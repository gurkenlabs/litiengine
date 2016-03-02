package de.gurkenlabs.litiengine.entities;

import java.util.Comparator;

public abstract class EntityComparator implements Comparator<IEntity> {
  private IEntity relativeEntity;

  protected EntityComparator() {

  }

  protected EntityComparator(final IEntity relativeEntity) {
    this.relativeEntity = relativeEntity;
  }

  public IEntity getRelativeEntity() {
    return this.relativeEntity;
  }

  public void setRelativeEntity(final IEntity relativeEntity) {
    this.relativeEntity = relativeEntity;
  }
}
