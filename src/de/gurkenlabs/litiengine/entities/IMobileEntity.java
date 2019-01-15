package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.physics.IMovementController;

/**
 * The Interface IMobileEntity.
 */
public interface IMobileEntity extends ICollisionEntity {
  /**
   * Gets a value that defines how long it takes the entity to reach the full
   * velocity (in ms).
   * 
   * @return the acceleration value
   */
  public int getAcceleration();

  /**
   * Gets a value that defines how long it takes the entity to stop when slowing down from movements (in ms).
   * 
   * @return the deceleration value
   */
  public int getDeceleration();

  /**
   * Gets the move destination.
   *
   * @return the move destination
   */
  public Point2D getMoveDestination();

  /**
   * Gets the entity's velocity in PIXELS per Second.
   *
   * @return the velocity in pixel per second.
   */
  public Attribute<Float> getVelocity();

  /**
   * Gets the entity's velocity in PIXELS per tick.
   *
   * @return The velocity in pixel per tick.
   */
  public float getTickVelocity();

  /**
   * Gets the movement controller.
   *
   * @return the movement controller
   */
  public IMovementController getMovementController();

  /**
   * Sets the acceleration for this entity. Acceleration is a value that defines how long it takes the entity to reach the full
   * velocity when starting to move (in ms). *
   * 
   * @param acceleration
   *          the new acceleration
   */
  public void setAcceleration(int acceleration);

  /**
   * Sets the deceleration for this entity. deceleration is a value that defines how long it takes the entity to stop when slowing down from movements (in ms).
   *
   * @param deceleration
   *          the new deceleration
   */
  public void setDeceleration(int deceleration);

  /**
   * Sets the point where the entity will be moved to.
   *
   * @param dest
   *          the destination point of the movement.
   */
  public void setMoveDestination(Point2D dest);

  /**
   * Sets the turn on move parameter for this entity. It specifies if the entity will change its angle to the direction of the move destination when moved.
   *
   * @param turn
   *          the new turn on move parameter.
   */
  public void setTurnOnMove(boolean turn);

  /**
   * Gets the turn on move parameter for this entity. It specifies if the entity will change its angle to the direction of the move destination when moved.
   *
   * @return true, if the entity will change its angle to the direction of the move destination when being moved
   */
  public boolean turnOnMove();
}
