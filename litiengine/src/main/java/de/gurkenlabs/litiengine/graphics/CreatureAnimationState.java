package de.gurkenlabs.litiengine.graphics;

/**
 * Represents the different animation states for a creature in the game.
 *
 * <p>The {@code CreatureAnimationState} enum defines three states:
 * <ul>
 *   <li>{@link #IDLE} - The creature is idle and not moving.</li>
 *   <li>{@link #MOVE} - The creature is moving.</li>
 *   <li>{@link #DEAD} - The creature is dead.</li>
 * </ul>
 *
 * <p>Each state is associated with a sprite string, which is derived from the
 * name of the state in lowercase.
 */
public enum CreatureAnimationState {
  IDLE,
  MOVE,
  DEAD;

  /**
   * The sprite string associated with the animation state.
   */
  private final String spriteString;

  /**
   * Initializes the {@code CreatureAnimationState} with its corresponding sprite string.
   *
   * <p>The sprite string is derived from the name of the state in lowercase.
   */
  CreatureAnimationState() {
    this.spriteString = name().toLowerCase();
  }

  /**
   * Retrieves the sprite string associated with this animation state.
   *
   * @return the sprite string for this state
   */
  public String spriteString() {
    return spriteString;
  }

  /**
   * Gets the opposite animation state for this state.
   *
   * <p>If the current state is {@link #IDLE}, the opposite is {@link #MOVE}.
   * Otherwise, the opposite is {@link #IDLE}.
   *
   * @return the opposite animation state
   */
  public CreatureAnimationState getOpposite() {
    return this == CreatureAnimationState.IDLE ? MOVE : IDLE;
  }
}
