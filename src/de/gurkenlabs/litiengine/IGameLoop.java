package de.gurkenlabs.litiengine;

import java.util.function.Consumer;

import de.gurkenlabs.core.ILaunchable;

public interface IGameLoop extends ILoop, ILaunchable {

  public long convertToMs(final long ticks);

  public long convertToTicks(final int ms);

  public int execute(int delay, Consumer<Integer> action);

  public int execute(int delay, Runnable action);

  public void updateExecutionTime(int index, long ticks);

  /**
   * Gets the time passed since the last tick in ms.
   *
   * @return
   */
  public long getDeltaTime();

  /**
   * Calculates the deltatime between the current game time and the specified
   * ticks in ms.
   *
   * @param ticks
   * @return The delta time in ms.
   */
  public long getDeltaTime(final long ticks);

  public long getTicks();

  public float getTimeScale();

  public int getUpdateRate();

  public void onUpsTracked(final Consumer<Integer> upsConsumer);

  public void setTimeScale(float timeScale);
}