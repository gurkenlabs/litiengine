package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.input.IMovementController;

public interface IMovableEntity extends ICollisionEntity {
  
  public float getFacingAngle();

  public Direction getFacingDirection();

  public IMovementController getMovementController();

  public float getVelocityInPixelsPerSecond();

  public boolean isIdle();

  public void onMoved(Consumer<IMovableEntity> consumer);

  /**
   * Sets the facing direction.
   *
   * @param orientation
   *          the new facing direction
   */
  public void setFacingAngle(float orientation);

  public void setFacingDirection(Direction facingDirection);

  /**
   * Sets the map location.
   *
   * @param location
   *          the new map location
   */
  public void setLocation(Point2D location);

  public void setMovementController(IMovementController movementController);
}
