package de.gurkenlabs.litiengine;

import java.util.function.Consumer;

import de.gurkenlabs.core.ILaunchable;

public interface IGameLoop extends ILaunchable {
  public long convertToMs(final long ticks);

  public long convertToTicks(final int ms);

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

  public GameTime getTime();

  public float getTimeScale();

  public int getUpdatablesCount();

  public int getUpdateRate();

  public void execute(int delay, Consumer<Long> action);

  public void onUpsTracked(final Consumer<Integer> upsConsumer);

  public void registerForUpdate(final IUpdateable updatable);

  public void setTimeScale(float timeScale);

  public void unregisterFromUpdate(final IUpdateable updatable);
}
