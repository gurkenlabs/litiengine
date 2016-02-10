package de.gurkenlabs.litiengine;

public class GameTime {
  private final IGameLoop gameLoop;

  public GameTime(final IGameLoop loop) {
    this.gameLoop = loop;
  }

  public long getYears() {
    return this.getMilliseconds() / 1000 / 60 / 60 / 24 / 365;
  }

  public long getDays() {
    return this.getMilliseconds() / 1000 / 60 / 60 / 24 % 365;
  }

  public long getHours() {
    return this.getMilliseconds() / 1000 / 60 / 60 % 24;
  }

  public long getMinutes() {
    return this.getMilliseconds() / 1000 / 60 % 60;
  }

  public long getSeconds() {
    return this.getMilliseconds() / 1000 % 60;
  }

  public long getMilliseconds() {
    return this.gameLoop.convertToMs(this.gameLoop.getTicks());
  }
}
