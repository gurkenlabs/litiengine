package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.core.IGameLoop;

public abstract class ImageEffect implements IImageEffect {
  private final IGameLoop gameLoop;
  private final int ttl;

  private final long aliveTick;

  protected ImageEffect(final IGameLoop gameLoop, int ttl) {
    this.gameLoop = gameLoop;
    this.ttl = ttl;
    this.aliveTick = this.gameLoop.getTicks();
  }

  @Override
  public int getAliveTime() {
    return (int) this.gameLoop.convertToMs(this.gameLoop.getTicks() - this.aliveTick);
  }

  @Override
  public int getTimeToLive() {
    return this.ttl;
  }

  @Override
  public boolean timeToLiveReached() {
    return this.getTimeToLive() > 0 && this.gameLoop.convertToMs(this.gameLoop.getTicks() - this.aliveTick) > this.getTimeToLive();
  }
}
