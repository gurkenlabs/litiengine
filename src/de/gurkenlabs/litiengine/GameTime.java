package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.EnvironmentLoadedListener;

public final class GameTime implements EnvironmentLoadedListener {
  private long environmentLoaded;

  protected GameTime() {
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
    return this.toMilliseconds(Game.loop().getTicks() - this.environmentLoaded);
  }

  /**
   * Converts the specified ticks to milliseconds using the game loop's update rate.
   * 
   * @param ticks
   *          The ticks that will be converted to milliseconds.
   * @return The milliseconds that correspond to the specified ticks.
   */
  public long toMilliseconds(final long ticks) {
    return this.toMilliseconds(ticks, Game.loop().getUpdateRate());
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
    return this.toTicks(milliseconds, Game.loop().getUpdateRate());
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