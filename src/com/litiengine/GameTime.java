package com.litiengine;

import com.litiengine.environment.Environment;
import com.litiengine.environment.EnvironmentLoadedListener;

/**
 * The {@code GameTime} class provides temporal information that can be used to perform time based events.
 *
 * <p>
 * The time provided by this class is measured in (game loop) ticks which is essentially an iteration of the game's main update loop.
 * </p>
 *
 * <p>
 * <b>Examples</b><br>
 * A common use-case is to track the passed time since a certain event occurred (e.g. some action was performed by an {@code Entity}).<br>
 * Another example is an environment that has a time limit.
 * </p>
 * 
 * @see GameLoop#getTickRate()
 */
public final class GameTime implements EnvironmentLoadedListener {
  private long environmentLoaded;

  GameTime() {
  }

  /**
   * Gets the current game time in ticks.
   * 
   * @return The current game time in ticks.
   * 
   * @see GameLoop#getTicks()
   */
  public long now() {
    return Game.loop().getTicks();
  }

  /**
   * Calculates the delta time between the current game time and the specified
   * ticks in milliseconds.
   *
   * @param tick
   *          The tick for which to calculate the delta time.
   * @return The delta time in ms.
   * 
   * @see #now()
   */
  public long since(final long tick) {
    return toMilliseconds(Game.loop().getTicks() - tick);
  }

  /**
   * Gets the time in milliseconds that has passed since the game has been started.<br>
   * This uses the configured update rate to calculate the passed time from the specified ticks.
   * 
   * @return The time since the game has been started.
   */
  public long sinceGameStart() {
    return this.toMilliseconds(Game.loop().getTicks());
  }

  /**
   * Get the time in milliseconds that has passed since the current environment was loaded.
   * 
   * @return The time since the current environment was loaded.
   */
  public long sinceEnvironmentLoad() {
    return this.since(this.environmentLoaded);
  }

  /**
   * Converts the specified ticks to milliseconds using the game loop's update rate.
   * 
   * @param ticks
   *          The ticks that will be converted to milliseconds.
   * @return The milliseconds that correspond to the specified ticks.
   */
  public long toMilliseconds(final long ticks) {
    return this.toMilliseconds(ticks, Game.loop().getTickRate());
  }

  /**
   * Converts the specified ticks to milliseconds using the specified update rate.
   * 
   * @param ticks
   *          The ticks that will be converted to milliseconds.
   * @param updateRate
   *          The updateRate that is used for the conversion.
   * @return The milliseconds that correspond to the specified ticks.
   */
  public long toMilliseconds(final long ticks, int updateRate) {
    return (long) (ticks / (updateRate / 1000.0));
  }

  /**
   * Converts the specified milliseconds to ticks using the game loop's update rate.
   * 
   * @param milliseconds
   *          The milliseconds that will be converted to ticks.
   * @return The ticks that correspond to the specified milliseconds.
   */
  public long toTicks(final int milliseconds) {
    return this.toTicks(milliseconds, Game.loop().getTickRate());
  }

  /**
   * Converts the specified milliseconds to ticks using the specified update rate.
   * 
   * @param milliseconds
   *          The milliseconds that will be converted to ticks.
   * @param updateRate
   *          The updateRate that is used for the conversion.
   * @return The ticks that correspond to the specified milliseconds.
   */
  public long toTicks(final int milliseconds, int updateRate) {
    return (long) (updateRate / 1000.0 * milliseconds);
  }

  @Override
  public void loaded(Environment environment) {
    environmentLoaded = Game.loop().getTicks();
  }
}