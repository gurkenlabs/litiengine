package com.litiengine.graphics;

public enum CreatureAnimationState {
  IDLE,
  WALK,
  DEAD;

  private final String spriteString;

  private CreatureAnimationState() {
    this.spriteString = this.name().toLowerCase();
  }

  public String spriteString() {
    return this.spriteString;
  }

  public CreatureAnimationState getOpposite() {
    return this == CreatureAnimationState.IDLE ? WALK : IDLE;
  }
}
