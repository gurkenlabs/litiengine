package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.physics.IEntityMovementController;

public interface IMovableEntity extends ICollisionEntity {
  public Point2D getMoveDestination();

  public IEntityMovementController getMovementController();

  /**
   * Gets the entitie's velocity in PIXEL / Second
   *
   * @return
   */
  public float getVelocity();

  public void onMoved(Consumer<IMovableEntity> consumer);

  /**
   * Sets the facing direction.
   *
   * @param orientation
   *          the new facing direction
   */
  public void setAngle(float angle);

  public void setMoveDestination(Point2D dest);

  public void setMovementController(IEntityMovementController movementController);

  public void setTurnOnMove(boolean turn);

  public boolean turnOnMove();
}
