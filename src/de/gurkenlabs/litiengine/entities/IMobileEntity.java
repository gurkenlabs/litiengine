package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.physics.IMovementController;

public interface IMobileEntity extends ICollisionEntity {

  /**
   * Adds the specified entity moved listener to receive events when this entity was moved.
   * <p>
   *   In comparison to the {@link EntityTransformListener#locationChanged(IEntity)} event, this provides some additional information
   *   about the movement (e.g. distance) and is only fired if the entity instance is currently loaded.
   * </p>
   * 
   * @param listener
   *          The listener to add.
   */
  void onMoved(EntityMovedListener listener);

  /**
   * Removes the specified entity moved listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  void removeMovedListener(EntityMovedListener listener);

  /**
   * Gets a value that defines how long it takes the entity to reach the full
   * velocity (in ms).
   * 
   * @return the acceleration value
   */
  int getAcceleration();

  /**
   * Gets a value that defines how long it takes the entity to stop when slowing down from movements (in ms).
   * 
   * @return the deceleration value
   */
  int getDeceleration();

  /**
   * Gets the entity's velocity in PIXELS per Second.
   *
   * @return the velocity in pixel per second.
   */
  Attribute<Float> getVelocity();

  /**
   * Gets the entity's velocity in PIXELS per tick.
   *
   * @return The velocity in pixel per tick.
   */
  float getTickVelocity();

  /**
   * Gets the movement controller.
   *
   * @return the movement controller
   */
  IMovementController movement();

  /**
   * Sets the acceleration for this entity. Acceleration is a value that defines how long it takes the entity to reach the full
   * velocity when starting to move (in ms). *
   * 
   * @param acceleration
   *          the new acceleration
   */
  void setAcceleration(int acceleration);

  /**
   * Sets the deceleration for this entity. deceleration is a value that defines how long it takes the entity to stop when slowing down from movements
   * (in ms).
   *
   * @param deceleration
   *          the new deceleration
   */
  void setDeceleration(int deceleration);

  /**
   * Sets the turn on move parameter for this entity. It specifies if the entity will change its angle to the direction of the move destination when
   * moved.
   *
   * @param turn
   *          the new turn on move parameter.
   */
  void setTurnOnMove(boolean turn);

  /**
   * Sets the base value on the velocity attribute of this instance.
   * 
   * @param velocity
   *          The velocity to be set.
   * 
   * @see #getVelocity()
   */
  void setVelocity(float velocity);

  /**
   * Gets the turn on move parameter for this entity. It specifies if the entity will change its angle to the direction of the move destination when
   * moved.
   *
   * @return true, if the entity will change its angle to the direction of the move destination when being moved
   */
  boolean turnOnMove();

  /**
   * This listener interface receives events when an entity was moved.
   * 
   * @see IMovementController
   * @see IMobileEntity#onMoved(EntityMovedListener)
   */
  @FunctionalInterface
  interface EntityMovedListener extends EventListener {
    /**
     * Invoked after an entity was moved.
     * 
     * @param event
     *          The entity moved event.
     */
    void moved(EntityMovedEvent event);
  }
}
