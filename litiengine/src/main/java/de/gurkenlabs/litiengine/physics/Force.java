package de.gurkenlabs.litiengine.physics;

import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class Force {
  private boolean cancelOnCollision;
  private boolean cancelOnReached;
  private boolean hasEnded;
  private Point2D location;
  private final float size;
  private float strength;
  private String identifier;

  /**
   * Instantiates a new force.
   *
   * @param location
   *          The location where the force is originating from
   * @param strength
   *          The strength/intensity of this force instance
   * @param size
   *          The size of this force (used to determine if/when an entity has reached the force)
   */
  public Force(final Point2D location, final float strength, final float size) {
    this.location = location;
    this.strength = strength;
    this.size = size;
    this.cancelOnCollision = true;
    this.cancelOnReached = true;
  }

  /**
   * Cancel on collision.
   *
   * @return true, if successful
   */
  public boolean cancelOnCollision() {
    return this.cancelOnCollision;
  }

  public boolean cancelOnReached() {
    return this.cancelOnReached;
  }

  public void end() {
    this.hasEnded = true;
  }

  /**
   * Gets the location.
   *
   * @return the location
   */
  public Point2D getLocation() {
    return this.location;
  }

  /**
   * Gets the strength in pixels per second.
   *
   * @return the strength in pixels per seconds
   */
  public float getStrength() {
    return this.strength;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  /**
   * Checks for ended.
   *
   * @return true, if successful
   */
  public boolean hasEnded() {
    return this.hasEnded;
  }

  public boolean hasReached(final ICollisionEntity entity) {
    return new Ellipse2D.Double(
        this.getLocation().getX() - this.size * 0.5,
        this.getLocation().getY() - this.size * 0.5,
        this.size,
        this.size)
            .intersects(entity.getCollisionBox());
  }

  public void setCancelOnCollision(final boolean cancelOnCollision) {
    this.cancelOnCollision = cancelOnCollision;
  }

  public void setCancelOnReached(final boolean cancelOnReached) {
    this.cancelOnReached = cancelOnReached;
  }

  public void setLocation(final Point2D location) {
    this.location = location;
  }

  public void setStrength(float strength) {
    this.strength = strength;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.identifier != null && !this.identifier.isEmpty() ? this.identifier : "Force");
    sb.append(": ");
    sb.append(this.getStrength());
    sb.append("px/sec; ");
    sb.append(this.getLocation());
    return sb.toString();
  }
}
