package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.Game;

public abstract class ImageEffect implements IImageEffect {
  private final int ttl;

  private final long aliveTick;

  protected ImageEffect(final int ttl) {
    this.ttl = ttl;
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
}
