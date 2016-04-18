package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface ICollisionEntity extends IEntity {
  /**
   * Gets the collision box.
   *
   * @return the collision box
   */
  public Rectangle2D getCollisionBox();

  /**
   * Gets the collision box.
   *
   * @param location
   *          the location
   * @return the collision box
   */
  public Rectangle2D getCollisionBox(Point2D location);

  /**
   * Checks for collision.
   *
   * @return true, if successful
   */
  public boolean hasCollision();

  /**
   * Sets the collision.
   *
   * @param collision
   *          the new collision
   */
  public void setCollision(boolean collision);

  public boolean collidesWith(ICollisionEntity otherEntity);
}
