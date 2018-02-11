package de.gurkenlabs.litiengine;

public class GameTime {

  protected GameTime() {
  }

  public long sinceGameStart() {
    return Game.getLoop().convertToMs(Game.getLoop().getTicks());
  }

  public long sinceEnvironmentLoad() {
    return Game.getLoop().convertToMs(Game.getLoop().getTicks() - Game.environmentLoadTick);
  }
}