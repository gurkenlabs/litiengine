package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.EnvironmentLoadedListener;

public final class GameTime implements EnvironmentLoadedListener {
  private long environmentLoaded;

  protected GameTime() {
  }

  public static long sinceGameStart() {
    return Game.loop().convertToMs(Game.loop().getTicks());
  }

  public long sinceEnvironmentLoad() {
    return Game.loop().convertToMs(Game.loop().getTicks() - this.environmentLoaded);
  }

  @Override
  public void loaded(Environment environment) {
    environmentLoaded = Game.loop().getTicks();
  }
}