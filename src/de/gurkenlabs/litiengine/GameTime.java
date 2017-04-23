package de.gurkenlabs.litiengine;

public class GameTime {
  private final IGameLoop gameLoop;

  public GameTime(final IGameLoop loop) {
    this.gameLoop = loop;
  }

  public long getDays() {
    return getDays(this.getMilliseconds());
  }

  public long getHours() {
    return getHours(this.getMilliseconds());
  }

  public long getMilliseconds() {
    return this.gameLoop.convertToMs(this.gameLoop.getTicks());
  }

  public long getMinutes() {
    return getMinutes(this.getMilliseconds());
  }

  public long getSeconds() {
    return getSeconds(this.getMilliseconds());
  }

  public long getYears() {
    return getYears(this.getMilliseconds());
  }

  public static long getDays(long ms) {
    return ms / 1000 / 60 / 60 / 24 % 365;
  }

  public static long getHours(long ms) {
    return ms / 1000 / 60 / 60 % 24;
  }

  public static long getMinutes(long ms) {
    return ms / 1000 / 60 % 60;
  }

  public static long getSeconds(long ms) {
    return ms / 1000 % 60;
  }

  public static long getMilliSeconds(long ms) {
    return ms % 1000;
  }

  public static long getYears(long ms) {
    return ms / 1000 / 60 / 60 / 24 / 365;
  }
}
