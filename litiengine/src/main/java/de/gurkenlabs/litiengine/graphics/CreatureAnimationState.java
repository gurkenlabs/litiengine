package de.gurkenlabs.litiengine.graphics;

public enum CreatureAnimationState {
  IDLE,
  WALK,
  DEAD;

  private final String spriteString;

  private CreatureAnimationState() {
    this.spriteString = name().toLowerCase();
  }

  public String spriteString() {
    return spriteString;
  }

  public CreatureAnimationState getOpposite() {
    return this == CreatureAnimationState.IDLE ? WALK : IDLE;
  }
}
