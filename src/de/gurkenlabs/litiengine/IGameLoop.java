package de.gurkenlabs.litiengine;

/**
 * The {@code IGameLoop} interface provides special methods for the game's main loop
 */
public interface IGameLoop extends ILoop {

  /**
   * Performs a timed action with the specified delay in ms.
   * 
   * @param delay
   *          The delay in milliseconds.
   * @param action
   *          The action to perform, once the delay has passed.
   * @return The id of the {@code TimedAction} that can be used to alter the execution time of the action or remove it.
   * 
   * @see IGameLoop#alterExecutionTime(int, long)
   */
  int perform(int delay, Runnable action);

  /**
   * Alters the execution time of the timed action with the specified index to the defined tick. This overwrites the originally specified delay.
   * 
   * @param id
   *          The id of the {@code TimedAction}.
   * @param tick
   *          The tick at which to perform the action instead.
   */
  void alterExecutionTime(int id, long tick);

  /**
   * Removes the {@code TimedAction} with the specified it.
   * 
   * @param id
   *          The id of the {@code TimedAction}.
   */
  void removeAction(int id);

  /**
   * Gets the game loop's current time scale (default = 1).
   * 
   * @return The game loop's current time scale.
   */
  float getTimeScale();

  /**
   * Sets the game loop's time scale.
   * <p>
   * This can be used to fast-forward the gameplay or to introduce slow-motion effects.
   * </p>
   * 
   * @param timeScale
   *          The time scale to set.
   */
  void setTimeScale(float timeScale);
}