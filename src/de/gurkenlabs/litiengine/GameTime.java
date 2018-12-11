package de.gurkenlabs.litiengine;

public class GameTime {

  protected GameTime() {
  }

  public long sinceGameStart() {
    return Game.loop().convertToMs(Game.loop().getTicks());
  }

  public long sinceEnvironmentLoad() {
    return Game.loop().convertToMs(Game.loop().getTicks() - Game.environmentLoadTick);
  }
}