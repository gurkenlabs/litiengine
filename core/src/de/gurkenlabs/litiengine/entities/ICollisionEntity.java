package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.physics.CollisionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface ICollisionEntity extends IEntity {
  void onCollision(CollisionListener listener);

  void removeCollisionListener(CollisionListener listener);

  void fireCollisionEvent(CollisionEvent event);

  boolean canCollideWith(ICollisionEntity otherEntity);

  /**
   * Gets the collision box.
   *
   * @return the collision box
   */
  Rectangle2D getCollisionBox();

  /**
   * Gets the collision box.
   *
   * @param location the location
   * @return the collision box
   */
  Rectangle2D getCollisionBox(Point2D location);

  /**
   * Gets the center {@link Point2D} of the entities collision box.
   *
   * @return The center {@link Point2D} of the entities collision box
   */
  Point2D getCollisionBoxCenter();

  Valign getCollisionBoxValign();

  Align getCollisionBoxAlign();

  Collision getCollisionType();

  double getCollisionBoxHeight();

  double getCollisionBoxWidth();

  /**
   * Checks for collision.
   *
   * @return true, if successful
   */
  boolean hasCollision();

  /**
   * Sets the collision.
   *
   * @param collision the new collision
   */
  void setCollision(boolean collision);

  void setCollisionBoxHeight(final double collisionBoxHeight);

  void setCollisionBoxWidth(final double collisionBoxWidth);

  void setCollisionBoxAlign(final Align align);

  void setCollisionBoxValign(final Valign valign);

  void setCollisionType(Collision collisionType);
}
