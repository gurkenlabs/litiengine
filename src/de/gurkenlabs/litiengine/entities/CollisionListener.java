package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.physics.CollisionEvent;
import de.gurkenlabs.litiengine.physics.PhysicsEngine;
import java.util.EventListener;

/** This listener provides callbacks for collision events on {@code ICollisionEntity}. */
@FunctionalInterface
public interface CollisionListener extends EventListener {

  /**
   * This method gets called after a collision has been resolved with the related {@code
   * ICollisionEntity}.
   *
   * <p>If the entity is considered to be the "active collider" of the collision (i.e. it was moved
   * with the {@code PhysicsEngine}), the event provides all other entities for which a collision
   * had to be resolved during the movement.
   *
   * <p>If an entity was just in a collision resolving process (i.e. was "touched" by an actively
   * moved collider), the event contains a reference to the collider instance.
   *
   * @param event The collision event.
   * @see PhysicsEngine#move(IMobileEntity, double, double)
   * @see CollisionEvent#getInvolvedEntities()
   */
  void collisionResolved(CollisionEvent event);
}
