package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.entities.IEntity;

public class EntityRotationImageEffect extends RotationImageEffect {
  private final IEntity entity;

  public EntityRotationImageEffect(final IEntity entity) {
    super(-1, 0);
    this.entity = entity;
  }

  @Override
  public float getAngle() {
    return 360 - this.entity.getAngle();
  }
}
