package de.gurkenlabs.litiengine.entities;

public interface IMobileCombatEntity extends ICombatEntity, IMobileEntity {
  public Direction getFacingDirection();

  public boolean isIdle();

  public void setFacingDirection(Direction facingDirection);

}