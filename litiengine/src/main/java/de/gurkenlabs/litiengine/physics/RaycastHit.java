package de.gurkenlabs.litiengine.physics;

import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import java.awt.geom.Point2D;

/**
 * Represents the result of a raycast in the physics engine. A RaycastHit contains the point of impact, the entity hit, and the distance to the impact
 * point.
 *
 * @param point    the point of impact
 * @param entity   the entity hit by the raycast
 * @param distance the distance to the point of impact
 */
public record RaycastHit(Point2D point, ICollisionEntity entity, double distance) {
}
