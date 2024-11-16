package de.gurkenlabs.litiengine.physics;

import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * Represents a force in the physics engine. A force has a location, strength, size, and various properties that determine its behavior.
 */
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
   * @param location The location where the force is originating from
   * @param strength The strength/intensity of this force instance
   * @param size     The size of this force (used to determine if/when an entity has reached the force)
   */
  public Force(final Point2D location, final float strength, final float size) {
    this.location = location;
    this.strength = strength;
    this.size = size;
    this.cancelOnCollision = true;
    this.cancelOnReached = true;
  }

  /**
   * Checks if the force should be canceled on collision.
   *
   * @return true if the force should be canceled on collision, false otherwise
   */
  public boolean cancelOnCollision() {
    return cancelOnCollision;
  }

  /**
   * Checks if the force should be canceled when the target is reached.
   *
   * @return true if the force should be canceled when the target is reached, false otherwise
   */
  public boolean cancelOnReached() {
    return cancelOnReached;
  }

  /**
   * Ends the force, marking it as ended.
   */
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

  /**
   * Gets the identifier for the force.
   *
   * @return the identifier of the force
   */
  public String getIdentifier() {
    return this.identifier;
  }

  /**
   * Checks if the force has ended.
   *
   * @return true if the force has ended, false otherwise
   */
  public boolean hasEnded() {
    return this.hasEnded;
  }

  /**
   * Checks if the force has reached the specified entity.
   *
   * @param entity the entity to check against
   * @return true if the force has reached the entity, false otherwise
   */
  public boolean hasReached(final ICollisionEntity entity) {
    return new Ellipse2D.Double(
      this.getLocation().getX() - this.size * 0.5,
      this.getLocation().getY() - this.size * 0.5,
      this.size,
      this.size)
      .intersects(entity.getCollisionBox());
  }

  /**
   * Sets whether the force should be canceled on collision.
   *
   * @param cancelOnCollision true if the force should be canceled on collision, false otherwise
   */
  public void setCancelOnCollision(final boolean cancelOnCollision) {
    this.cancelOnCollision = cancelOnCollision;
  }

  /**
   * Sets whether the force should be canceled when the target is reached.
   *
   * @param cancelOnReached true if the force should be canceled when the target is reached, false otherwise
   */
  public void setCancelOnReached(final boolean cancelOnReached) {
    this.cancelOnReached = cancelOnReached;
  }

  /**
   * Sets the location of the force.
   *
   * @param location the new location of the force
   */
  public void setLocation(final Point2D location) {
    this.location = location;
  }

  /**
   * Sets the strength of the force in pixels per second.
   *
   * @param strength the new strength of the force
   */
  public void setStrength(float strength) {
    this.strength = strength;
  }

  /**
   * Sets the identifier for the force.
   *
   * @param identifier the new identifier for the force
   */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /**
   * Returns a string representation of the Force object. The string includes the identifier (if available), strength in pixels per second, and
   * location.
   *
   * @return a string representation of the Force object
   */
  @Override
  public String toString() {
    return (this.identifier != null && !this.identifier.isEmpty() ? this.identifier : "Force")
      + ": "
      + this.getStrength()
      + "px/sec; "
      + this.getLocation();
  }
}
