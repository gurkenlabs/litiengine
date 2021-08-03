package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ITimeToLive;
import java.awt.image.BufferedImage;

public abstract class ImageEffect implements ITimeToLive, Comparable<ImageEffect> {
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
    this.aliveTick = Game.time().now();
  }

  @Override
  public long getAliveTime() {
    return Game.time().since(this.aliveTick);
  }

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

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int compareTo(ImageEffect other) {
    return Integer.compare(this.getPriority(), other.getPriority());
  }

  public abstract BufferedImage apply(BufferedImage image);
}
