package de.gurkenlabs.litiengine.entities;

public interface IMovableCombatEntity extends ICombatEntity, IMovableEntity {
  public boolean isIdle();

  public void setFacingDirection(Direction facingDirection);

  public Direction getFacingDirection();

}