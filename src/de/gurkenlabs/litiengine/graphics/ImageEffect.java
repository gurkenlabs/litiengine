package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.Game;

public abstract class ImageEffect implements IImageEffect {
  private final long aliveTick;

  private String name;
  private final int ttl;
  private int priority;

  protected ImageEffect(final String name) {
    this(0, name);
  }

  protected ImageEffect(final int ttl, final String name) {
    this.ttl = ttl;
    this.name = name;
    this.aliveTick = Game.loop().getTicks();
  }

  @Override
  public long getAliveTime() {
    return Game.loop().getDeltaTime(this.aliveTick);
  }

  @Override
  public String getName() {
    return this.name;
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
  public int getPriority() {
    return priority;
  }

  @Override
  public void setPriority(int priority) {
    this.priority = priority;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int compareTo(IImageEffect other) {
    return Integer.compare(this.getPriority(), other.getPriority());
  }
}
