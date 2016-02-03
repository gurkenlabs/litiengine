package de.gurkenlabs.litiengine;

import java.util.function.Consumer;

import de.gurkenlabs.core.ILaunchable;


public interface IGameLoop extends ILaunchable {
  public void onUpsTracked(final Consumer<Integer> upsConsumer);

  public void registerForUpdate(final IUpdateable updatable);

  public void unregisterFromUpdate(final IUpdateable updatable);

  public long getTicks();

  public long convertToMs(final long ticks);

  public long getDeltaTime();

  /**
   * Calculates the deltatime between the current game time and the specified
   * ticks in ms.
   *
   * @param ticks
   * @return The delta time in ms.
   */
  public long getDeltaTime(final long ticks);
}
