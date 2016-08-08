package de.gurkenlabs.litiengine;

import java.util.function.Consumer;

import de.gurkenlabs.core.ILaunchable;

public interface IGameLoop extends ILaunchable {
  public long convertToMs(final long ticks);

  public int getUpdateRate();

  /**
   * Gets the time passed since the last tick.
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

  public GameTime getTime();

  public float getTimeScale();

  public void setTimeScale(float timeScale);

  public long getTicks();

  public void onUpsTracked(final Consumer<Integer> upsConsumer);

  public void registerForUpdate(final IUpdateable updatable);

  public void unregisterFromUpdate(final IUpdateable updatable);

  public int getUpdatablesCount();
}
