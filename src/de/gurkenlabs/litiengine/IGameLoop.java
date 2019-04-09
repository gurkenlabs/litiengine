package de.gurkenlabs.litiengine;

public interface IGameLoop extends ILoop {

  public int execute(int delay, Runnable action);

  public void updateExecutionTime(int index, long ticks);

  /**
   * Gets the time that passed since the last tick in ms.
   *
   * @return The delta time in ms.
   */
  public long getDeltaTime();

  /**
   * Calculates the deltatime between the current game time and the specified
   * ticks in ms.
   *
   * @param ticks
   *          The ticks for which to calculate the delta time.
   * @return The delta time in ms.
   */
  public long getDeltaTime(final long ticks);

  public long getTicks();

  public float getTimeScale();

  public int getUpdateRate();

  public void setTimeScale(float timeScale);
}