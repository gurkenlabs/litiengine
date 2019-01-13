package de.gurkenlabs.litiengine.graphics;

public enum CreatureAnimationState {
  IDLE,
  WALK,
  DEAD;

  public String spriteString() {
    return this.name().toLowerCase();
  }

  public CreatureAnimationState getOpposite() {
    return this == CreatureAnimationState.IDLE ? WALK : IDLE;
  }
}
