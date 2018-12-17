package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.environment.EnvironmentLoadedListener;
import de.gurkenlabs.litiengine.environment.IEnvironment;

public final class GameTime implements EnvironmentLoadedListener {
  private long environmentLoaded;

  protected GameTime() {
  }

  public long sinceGameStart() {
    return Game.loop().convertToMs(Game.loop().getTicks());
  }

  public long sinceEnvironmentLoad() {
    return Game.loop().convertToMs(Game.loop().getTicks() - this.environmentLoaded);
  }

  @Override
  public void environmentLoaded(IEnvironment environment) {
    environmentLoaded = Game.loop().getTicks();
  }
}