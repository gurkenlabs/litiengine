package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.physics.CollisionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface ICollisionEntity extends IEntity {
  /**
   * Registers a {@link CollisionListener} to this entity. The listener will be notified whenever this entity collides with another
   * {@link ICollisionEntity}.
   *
   * @param listener The {@link CollisionListener} to be added to this entity. It should not be {@code null}.
   */
  void onCollision(CollisionListener listener);

  /**
   * Removes a previously registered {@link CollisionListener} from this entity. After removal, the listener will no longer receive collision events
   * involving this entity.
   *
   * @param listener The {@link CollisionListener} to be removed. It should match a listener that was previously added with
   *                 {@link #onCollision(CollisionListener)}.
   */
  void removeCollisionListener(CollisionListener listener);

  /**
   * Triggers a collision event for this entity. This method is used to manually fire a collision event, which can be useful for custom collision
   * handling or for simulating collisions in specific scenarios.
   *
   * @param event The {@link CollisionEvent} that encapsulates the details of the collision. This includes information such as the entities involved
   *              in the collision and the collision's impact point.
   */
  void fireCollisionEvent(CollisionEvent event);

  /**
   * Determines if this entity can collide with another specified {@link ICollisionEntity}. This method should be implemented to define custom
   * collision logic between entities.
   *
   * @param otherEntity The other {@link ICollisionEntity} to check collision capability with.
   * @return {@code true} if this entity can collide with the specified entity; {@code false} otherwise.
   */
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

  /**
   * Retrieves the vertical alignment of the entity's collision box. This alignment determines the vertical positioning of the collision box relative
   * to the entity's location.
   *
   * @return The {@link Valign} representing the vertical alignment of the collision box.
   */
  Valign getCollisionBoxValign();

  /**
   * Retrieves the horizontal alignment of the entity's collision box. This alignment determines the horizontal positioning of the collision box
   * relative to the entity's location.
   *
   * @return The {@link Align} representing the horizontal alignment of the collision box.
   */
  Align getCollisionBoxAlign();

  /**
   * Retrieves the collision type of this entity. The collision type defines how this entity interacts with other entities in terms of physical
   * collisions.
   *
   * @return The {@link Collision} type of this entity, indicating how it should be treated in collision detection and handling.
   */
  Collision getCollisionType();

  /**
   * Retrieves the height of the entity's collision box. This height is used in collision detection to determine the vertical bounds of the entity.
   *
   * @return The height of the collision box in pixels.
   */
  double getCollisionBoxHeight();

  /**
   * Retrieves the width of the entity's collision box. This width is used in collision detection to determine the horizontal bounds of the entity.
   *
   * @return The width of the collision box in pixels.
   */
  double getCollisionBoxWidth();

  /**
   * Checks if the entity has collision enabled.
   *
   * @return {@code true} if the entity has collision enabled; {@code false} otherwise.
   */
  boolean hasCollision();

  /**
   * Sets the collision state of the entity.
   *
   * @param collision The collision state to set. {@code true} to enable collision; {@code false} to disable it.
   */
  void setCollision(boolean collision);

  /**
   * Sets the height of the entity's collision box.
   *
   * @param collisionBoxHeight The height of the collision box in pixels.
   */
  void setCollisionBoxHeight(final double collisionBoxHeight);

  /**
   * Sets the width of the entity's collision box.
   *
   * @param collisionBoxWidth The width of the collision box in pixels.
   */
  void setCollisionBoxWidth(final double collisionBoxWidth);

  /**
   * Sets the horizontal alignment of the entity's collision box.
   *
   * @param align The {@link Align} representing the horizontal alignment of the collision box.
   */
  void setCollisionBoxAlign(final Align align);

  /**
   * Sets the vertical alignment of the entity's collision box.
   *
   * @param valign The {@link Valign} representing the vertical alignment of the collision box.
   */
  void setCollisionBoxValign(final Valign valign);

  /**
   * Sets the collision type of this entity.
   *
   * @param collisionType The {@link Collision} type indicating how this entity should be treated in collision detection and handling.
   */
  void setCollisionType(Collision collisionType);
}
