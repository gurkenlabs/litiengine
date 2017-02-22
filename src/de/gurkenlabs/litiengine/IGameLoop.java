package de.gurkenlabs.litiengine;

import java.util.function.Consumer;

import de.gurkenlabs.core.ILaunchable;

public interface IGameLoop extends ILaunchable {
  /**
   * Attaches the update method of the specified
   * {@link de.gurkenlabs.litiengine.IUpdatable IUpdatable} instance to be
   * called every tick. The tick rate can be configured in the client
   * configuration and is independant from rendering.
   * 
   * @param updatable
   */
  public void attach(final IUpdateable updatable);

  public long convertToMs(final long ticks);

  public long convertToTicks(final int ms);

  /**
   * Detaches the specified instance from the game loop.
   * 
   * @param updatable
   */
  public void detach(final IUpdateable updatable);

  public void execute(int delay, Consumer<Long> action);

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

  public int getUpdateRate();

  public void onUpsTracked(final Consumer<Integer> upsConsumer);

  public void setTimeScale(float timeScale);
}