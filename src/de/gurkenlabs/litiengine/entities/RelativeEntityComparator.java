package de.gurkenlabs.litiengine.entities;

import java.util.Comparator;

public abstract class RelativeEntityComparator implements Comparator<IEntity> {
  private IEntity relativeEntity;

  protected RelativeEntityComparator() {}

  protected RelativeEntityComparator(final IEntity relativeEntity) {
    this.relativeEntity = relativeEntity;
  }

  public IEntity getRelativeEntity() {
    return this.relativeEntity;
  }

  public void setRelativeEntity(final IEntity relativeEntity) {
    this.relativeEntity = relativeEntity;
  }
}
