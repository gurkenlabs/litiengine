package de.gurkenlabs.litiengine;

import de.gurkenlabs.util.TimeUtilities;

public class GameTime {
  private final IGameLoop gameLoop;

  public GameTime(final IGameLoop loop) {
    this.gameLoop = loop;
  }

  public long getDays() {
    return TimeUtilities.getDays(this.getMilliseconds());
  }

  public long getHours() {
    return TimeUtilities.getHours(this.getMilliseconds());
  }

  public long getMilliseconds() {
    return this.gameLoop.convertToMs(this.gameLoop.getTicks());
  }

  public long getMinutes() {
    return TimeUtilities.getMinutes(this.getMilliseconds());
  }

  public long getSeconds() {
    return TimeUtilities.getSeconds(this.getMilliseconds());
  }

  public long getYears() {
    return TimeUtilities.getYears(this.getMilliseconds());
  }
}
