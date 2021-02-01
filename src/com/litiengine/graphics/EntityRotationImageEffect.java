package com.litiengine.graphics;

import com.litiengine.entities.IEntity;

public class EntityRotationImageEffect extends RotationImageEffect {
  private final IEntity entity;

  /**
   * Initializes a new instance of the {@code EntityRotationImageEffect}.
   *
   * @param entity
   *          The entity to which this affect will be applied.
   */
  public EntityRotationImageEffect(final IEntity entity) {
    super(-1, 0);
    this.entity = entity;
  }

  @Override
  public double getAngle() {
    return 360 - this.entity.getAngle();
  }
}
