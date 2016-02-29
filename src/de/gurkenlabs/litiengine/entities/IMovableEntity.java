package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.physics.IEntityMovementController;

public interface IMovableEntity extends ICollisionEntity {
  /**
   * Gets the entitie's velocity in PIXEL / Second
   * 
   * @return
   */
  public float getVelocity();

  public IEntityMovementController getMovementController();

  public void onMoved(Consumer<IMovableEntity> consumer);

  /**
   * Sets the facing direction.
   *
   * @param orientation
   *          the new facing direction
   */
  public void setAngle(float angle);

  /**
   * Sets the map location.
   *
   * @param location
   *          the new map location
   */
  public void setLocation(Point2D location);

  public void setMovementController(IEntityMovementController movementController);
}
