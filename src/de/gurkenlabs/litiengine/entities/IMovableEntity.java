package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.function.Consumer;

public interface IMovableEntity extends ICollisionEntity {
  /**
   * Gets a value that defines how long it takes the entity to reach the full
   * velocity (in ms).
   * 
   * @return
   */
  public int getAcceleration();

  /**
   * Gets a value that defines how long it takes the entity to stop (in ms).
   * 
   * @return
   */
  public int getDeceleration();

  public Point2D getMoveDestination();

  /**
   * Gets the entitie's velocity in PIXEL / Second
   *
   * @return
   */
  public float getVelocity();

  public void onMoved(Consumer<IMovableEntity> consumer);

  public void setAcceleration(int acceleration);

  public void setAngle(float angle);

  public void setDeceleration(int deceleration);

  public void setMoveDestination(Point2D dest);

  public void setTurnOnMove(boolean turn);

  public void setVelocity(short velocity);

  public boolean turnOnMove();
}
