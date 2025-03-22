package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ITimeToLive;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Represents an abstract image effect that can be applied to a BufferedImage. Implements ITimeToLive and Comparable interfaces.
 */
public abstract class ImageEffect implements ITimeToLive, Comparable<ImageEffect> {
  private final long aliveTick;
  private final int ttl;
  private String name;
  private int priority;

  /**
   * Constructs a new ImageEffect with the specified name and a time-to-live of 0.
   *
   * @param name the name of the image effect
   */
  protected ImageEffect(final String name) {
    this(0, name);
  }

  /**
   * Constructs a new ImageEffect with the specified time-to-live and name.
   *
   * @param ttl  the time-to-live of the image effect in milliseconds
   * @param name the name of the image effect
   */
  protected ImageEffect(final int ttl, final String name) {
    this.ttl = ttl;
    this.name = name;
    this.aliveTick = Game.time().now();
  }

  /**
   * Gets the time in milliseconds that this effect has been alive.
   *
   * @return the alive time in milliseconds
   */
  @Override
  public long getAliveTime() {
    return Game.time().since(this.aliveTick);
  }

  /**
   * Gets the name of this image effect.
   *
   * @return the name of the image effect
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of this image effect.
   *
   * @param name the new name of the image effect
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the time-to-live of this image effect in milliseconds.
   *
   * @return the time-to-live in milliseconds
   */
  @Override
  public int getTimeToLive() {
    return this.ttl;
  }

  /**
   * Checks if the time-to-live of this image effect has been reached.
   *
   * @return true if the time-to-live has been reached, false otherwise
   */
  @Override
  public boolean timeToLiveReached() {
    return this.getTimeToLive() > 0 && this.getAliveTime() > this.getTimeToLive();
  }

  /**
   * Gets the priority of this image effect.
   *
   * @return the priority of the image effect
   */
  public int getPriority() {
    return priority;
  }

  /**
   * Sets the priority of this image effect.
   *
   * @param priority the new priority of the image effect
   */
  public void setPriority(int priority) {
    this.priority = priority;
  }

  /**
   * Compares this image effect with another image effect based on their priority.
   *
   * @param other the other image effect to compare to
   * @return a negative integer, zero, or a positive integer as this image effect's priority is less than, equal to, or greater than the other image
   * effect's priority
   */
  @Override
  public int compareTo(ImageEffect other) {
    return Integer.compare(this.getPriority(), other.getPriority());
  }

  /**
   * Checks if this image effect is equal to another object. Two image effects are considered equal if they have the same name and priority.
   *
   * @param obj the object to compare to
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ImageEffect that = (ImageEffect) obj;
    return priority == that.priority && Objects.equals(name, that.name);
  }

  /**
   * Returns the hash code of this image effect.
   *
   * @return the hash code of the image effect
   */
  @Override
  public int hashCode() {
    return Objects.hash(name, priority);
  }

  /**
   * Applies this image effect to the specified BufferedImage.
   *
   * @param image the BufferedImage to apply the effect to
   * @return the BufferedImage with the effect applied
   */
  public abstract BufferedImage apply(BufferedImage image);
}
