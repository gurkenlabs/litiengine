package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.Game;

public abstract class ImageEffect implements IImageEffect {
  private final int ttl;

  private final long aliveTick;
  private final String name;

  protected ImageEffect(final int ttl, final String name) {
    this.ttl = ttl;
    this.name = name;
    this.aliveTick = Game.getLoop().getTicks();
  }

  @Override
  public long getAliveTime() {
    return Game.getLoop().getDeltaTime(this.aliveTick);
  }

  @Override
  public int getTimeToLive() {
    return this.ttl;
  }

  @Override
  public boolean timeToLiveReached() {
    return this.getTimeToLive() > 0 && this.getAliveTime() > this.getTimeToLive();
  }

  @Override
  public String getName() {
    return this.name;
  }
}
