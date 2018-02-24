package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.function.Consumer;

public interface IMobileEntity extends ICollisionEntity {
  /**
   * Gets a value that defines how long it takes the entity to reach the full
   * velocity (in ms).
   * 
   * @return the acceleration value
   */
  public int getAcceleration();

  /**
   * Gets a value that defines how long it takes the entity to stop (in ms).
   * 
   * @return the deceleration value
   */
  public int getDeceleration();

  public Point2D getMoveDestination();

  /**
   * Gets the entitie's velocity in PIXEL / Second
   *
   * @return the velocity in pixel per second
   */
  public float getVelocity();

  public void onMoved(Consumer<IMobileEntity> consumer);

  public void setAcceleration(int acceleration);

  public void setAngle(float angle);

  public void setDeceleration(int deceleration);

  public void setMoveDestination(Point2D dest);

  public void setTurnOnMove(boolean turn);

  public void setVelocity(short velocity);

  public boolean turnOnMove();
}
