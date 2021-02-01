package com.litiengine.physics;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.litiengine.Game;
import com.litiengine.entities.ICollisionEntity;
import com.litiengine.entities.IMobileEntity;
import com.litiengine.Direction;
import com.litiengine.IUpdateable;
import com.litiengine.util.ArrayUtilities;
import com.litiengine.util.MathUtilities;
import com.litiengine.util.geom.GeometricUtilities;

/**
 * This class is used to hold all collision aware instances and static collision boxes.
 * It is responsible for resolving movement that respects the collision boxes in the game. This is achieved by the <b>{@code move}</b> method
 * group.
 * <br>
 * The <b>{@code collides}</b> method group can detect a collision at a certain location, for rectangles, or collision aware entities.
 * Also, there's an overload that takes a {@code Line2D} to perform a basic raycast check.
 */
public final class PhysicsEngine implements IUpdateable {
  private Rectangle2D environmentBounds;

  private final Map<Collision, List<ICollisionEntity>> collisionEntities = new ConcurrentHashMap<>();
  private final Map<Collision, List<Rectangle2D>> collisionBoxes = new ConcurrentHashMap<>();

  /**
   * <p>
   * <b>You should never call this manually! Instead use the {@code Game.physics()} instance.</b>
   * </p>
   *
   * @see Game#physics()
   */
  public PhysicsEngine() {
    if (Game.physics() != null) {
      throw new UnsupportedOperationException("Never initialize a PhysicsEngine manually. Use Game.physics() instead.");
    }

    this.collisionEntities.put(Collision.DYNAMIC, new CopyOnWriteArrayList<>());
    this.collisionEntities.put(Collision.STATIC, new CopyOnWriteArrayList<>());
    this.collisionEntities.put(Collision.ANY, new CopyOnWriteArrayList<>());

    this.collisionBoxes.put(Collision.DYNAMIC, new CopyOnWriteArrayList<>());
    this.collisionBoxes.put(Collision.STATIC, new CopyOnWriteArrayList<>());
    this.collisionBoxes.put(Collision.ANY, new CopyOnWriteArrayList<>());
  }

  /**
   * Adds the specified collision aware entity to the physics engine which will make it respect the entity's collision box for upcoming calls.
   *
   * <p>
   * <i>If you add a {@code ICollisionEntiy} to your Environment, it will automatically be added to the the PhysicsEngine. There is typically no
   * need to call this explicitly.</i>
   * </p>
   *
   * @param entity The collision entity to be added.
   * @see ICollisionEntity#getCollisionBox()
   * @see PhysicsEngine#remove(ICollisionEntity)
   */
  public void add(final ICollisionEntity entity) {
    if (entity.getCollisionType() == null) {
      return;
    }

    switch (entity.getCollisionType()) {
    case DYNAMIC:
      break;
    case STATIC:
      this.collisionEntities.get(entity.getCollisionType()).add(entity);
      break;
    default:
      return;
    }

    this.collisionEntities.get(Collision.ANY).add(entity);
  }

  /**
   * Removes the specified entity from any collision processing. Typically this method is implicitly called when an entity is removed from the current
   * environment.
   *
   * @param entity The entity that is about to be removed.
   */
  public void remove(final ICollisionEntity entity) {
    if (entity.getCollisionType() == null) {
      return;
    }

    switch (entity.getCollisionType()) {
    case DYNAMIC:
      break;
    case STATIC:
      this.collisionEntities.get(entity.getCollisionType()).remove(entity);
      break;
    default:
      return;
    }

    this.collisionEntities.get(Collision.ANY).remove(entity);
  }

  /**
   * Clears all previously registered participants in the collision process from this instance.
   * This includes all entities, static collision boxes and the map boundaries.
   */
  public void clear() {
    for (Collision type : Collision.values()) {
      if (type == Collision.NONE) {
        continue;
      }

      this.collisionEntities.get(type).clear();
      this.collisionBoxes.get(type).clear();
    }

    this.setBounds(null);
  }

  /**
   * Gets all {@code CollisionBoxes}, regardless of their {@code Collision} type.
   *
   * @return A {@code Collection} of all {@code CollisionBox}es registered on the {@code PhysicsEngine}.
   */
  public Collection<Rectangle2D> getCollisionBoxes() {
    return this.getCollisionBoxes(Collision.ANY);
  }

  /**
   * Gets all {@code CollisionBoxes} with the given {@code Collision} type.
   *
   * @param type The {@code Collision} type by which the {@code CollisionBoxes} are selected.
   * @return If the {@code Collision} type is {@code NONE}, return an empty set. Otherwise, a {@code Collection} of all {@code CollisionBoxes}
   * registered on the {@code PhysicsEngine} that have the given {@code Collision} type.
   */
  public Collection<Rectangle2D> getCollisionBoxes(Collision type) {
    if (type == Collision.NONE) {
      return Collections.emptySet();
    }

    return Collections.unmodifiableCollection(this.collisionBoxes.get(type));
  }

  /**
   * Gets all {@code ICollisionEntities}, regardless of their {@code Collision} type.
   *
   * @return A {@code Collection} of all {@code ICollisionEntities} registered on the {@code PhysicsEngine}.
   */
  public Collection<ICollisionEntity> getCollisionEntities() {
    return this.getCollisionEntities(Collision.ANY);
  }

  /**
   * Gets all {@code ICollisionEntities} with the given {@code Collision} type.
   *
   * @param type The {@code Collision} type by which the {@code ICollisionEntities} are selected.
   * @return If the {@code Collision} type is {@code NONE}, return an empty set. Otherwise, a {@code Collection} of all {@code ICollisionEntities}
   * registered on the {@code PhysicsEngine} that have the given {@code Collision} type.
   */
  public Collection<ICollisionEntity> getCollisionEntities(Collision type) {
    if (type == Collision.NONE) {
      return Collections.emptySet();
    }

    return Collections.unmodifiableCollection(this.collisionEntities.get(type));
  }

  /**
   * Gets the environment bounds that confine the operation area of the {@code PhysicsEngine}.
   *
   * @return The {@code Rectangle2D} confining the operation area of the {@code PhysicsEngine}.
   */
  public Rectangle2D getBounds() {
    return this.environmentBounds;
  }

  /**
   * Sets the environment bounds that confine the operation area of the {@code PhysicsEngine}.
   *
   * @param environmentBounds The {@code Rectangle2D} confining the operation area of the {@code PhysicsEngine}.
   */
  public void setBounds(final Rectangle2D environmentBounds) {
    this.environmentBounds = environmentBounds;
  }

  /**
   * Checks if a given line collides with anything registered in the {@code PhysicsEngine}.
   *
   * @param line The {@code Line2D} to check for collision.
   * @return {@code true} if the line collides with anything. {@code false} otherwise.
   */
  public boolean collides(Line2D line) {
    return this.collides(line, Collision.ANY, null);
  }

  /**
   * Checks if a line collides with anything of the given {@code Collision} type.
   *
   * @param line      The {@code Line2D} to check for collision.
   * @param collision The {@code Collision} type to check for collisions.
   * @return {@code true} if the line collides with anything of the given {@code Collision} type. {@code false} otherwise.
   * @see Collision
   */
  public boolean collides(Line2D line, Collision collision) {
    return this.collides(line, collision, null);
  }

  /**
   * Checks if a given {@code ICollisionEntity} collides with anything that intersects a specific line.
   *
   * @param line   The {@code Line2D} to check for collision.
   * @param entity The {@code ICollisionEntity} to check for collision.
   * @return {@code true} if any {@code ICollisionEntity} intersecting the line collides with the given {@code ICollisionEntity}. {@code false}
   * otherwise.
   * @see Collision
   * @see ICollisionEntity
   */
  public boolean collides(Line2D line, ICollisionEntity entity) {
    return this.collides(line, Collision.ANY, entity);
  }

  /**
   * Checks if a given {@code ICollisionEntity} collides with any {@code ICollisionEntities} of a given {@code Collision} type that intersect a
   * specific line.
   *
   * @param line      The {@code Line2D} to check for collision.
   * @param collision The {@code Collision} type to check for collision.
   * @param entity    The {@code ICollisionEntity} to check for collision.
   * @return {@code true} if the entity collides with any {@code ICollisionEntity} on the given line. {@code false} otherwise.
   * @see Collision
   * @see ICollisionEntity
   */
  public boolean collides(final Line2D line, Collision collision, ICollisionEntity entity) {
    return this.collides(entity, collision, e -> GeometricUtilities.getIntersectionPoint(line, e.getCollisionBox()) != null);
  }

  /**
   * Checks if a given rectangle collides with anything registered in the {@code PhysicsEngine}.
   *
   * @param rect The {@code Rectangle2D} to check for collision.
   * @return {@code true} if the rectangle collides with anything. {@code false} otherwise.
   */
  public boolean collides(final Rectangle2D rect) {
    return this.collides(rect, Collision.ANY);
  }

  /**
   * Checks if a given {@code ICollisionEntity} collides with anything that intersects a specific rectangle.
   *
   * @param rect   The {@code Rectangle2D} to check for collision.
   * @param entity The {@code ICollisionEntity} to check for collision.
   * @return {@code true} if the entity collides with any {@code ICollisionEntity} in the given rectangle. {@code false} otherwise.
   * @see ICollisionEntity
   */
  public boolean collides(Rectangle2D rect, ICollisionEntity entity) {
    return this.collides(rect, Collision.ANY, entity);
  }

  /**
   * Checks if a rectangle collides with anything of the given {@code Collision} type.
   *
   * @param rect      The {@code Rectangle2D} to check for collision.
   * @param collision The {@code Collision} type to check for collisions.
   * @return {@code true} if the rectangle collides with anything of the given {@code Collision} type. {@code false} otherwise.
   * @see Collision
   */
  public boolean collides(Rectangle2D rect, Collision collision) {
    return collides(rect, collision, null);
  }

  /**
   * Checks if a given {@code ICollisionEntity} collides with any {@code ICollisionEntities} of a given {@code Collision} type that intersect a
   * specific rectangle.
   *
   * @param rect      The {@code Rectangle2D} to check for collision.
   * @param collision The {@code Collision} type to check for collision.
   * @param entity    The {@code ICollisionEntity} to check for collision.
   * @return {@code true} if the entity collides with any {@code ICollisionEntity} in the given rectangle. {@code false} otherwise.
   * @see Collision
   * @see ICollisionEntity
   */
  public boolean collides(Rectangle2D rect, Collision collision, ICollisionEntity entity) {
    if (this.environmentBounds != null && !this.environmentBounds.intersects(rect)) {
      return true;
    }

    return collides(entity, collision, otherEntity -> GeometricUtilities.intersects(otherEntity.getCollisionBox(), rect));
  }

  /**
   * Checks if a given point collides with anything registered in the {@code PhysicsEngine}.
   *
   * @param location The {@code Point2D} to check for collision.
   * @return {@code true} if the point collides with anything. {@code false} otherwise.
   */
  public boolean collides(final Point2D location) {
    return this.collides(location, Collision.ANY);
  }

  /**
   * Checks if a point collides with anything of the given {@code Collision} type.
   *
   * @param location  The {@code Point2D} to check for collision.
   * @param collision The {@code Collision} type to check for collisions.
   * @return {@code true} if the point collides with anything of the given {@code Collision} type. {@code false} otherwise.
   * @see Collision
   */
  public boolean collides(Point2D location, Collision collision) {
    return collides(location, collision, null);
  }

  /**
   * Checks if a given {@code ICollisionEntity} collides with anything that intersects a specific point.
   *
   * @param location The {@code Point2D} to check for collision.
   * @param entity   The {@code ICollisionEntity} to check for collision.
   * @return {@code true} if the entity collides with any {@code ICollisionEntity} on the given point. {@code false} otherwise.
   * @see ICollisionEntity
   */
  public boolean collides(Point2D location, ICollisionEntity entity) {
    return this.collides(location, Collision.ANY, entity);
  }

  /**
   * Checks if a given {@code ICollisionEntity} collides with any {@code ICollisionEntities} of a given {@code Collision} type that intersect a
   * specific point.
   *
   * @param location  The {@code Point2D} to check for collision.
   * @param collision The {@code Collision} type to check for collision.
   * @param entity    The {@code ICollisionEntity} to check for collision.
   * @return {@code true} if the entity collides with any {@code ICollisionEntity} on the given point. {@code false} otherwise.
   * @see Collision
   * @see ICollisionEntity
   */
  public boolean collides(Point2D location, Collision collision, ICollisionEntity entity) {
    if (this.environmentBounds != null && !this.environmentBounds.contains(location)) {
      return true;
    }

    return collides(entity, collision, otherEntity -> otherEntity.getCollisionBox().contains(location));
  }

  /**
   * Checks if the point at the given coordinates collides with anything registered in the {@code PhysicsEngine}.
   *
   * @param x The x coordinate to check for collision.
   * @param y The y coordinate to check for collision.
   * @return {@code true} if the coordinates collide with anything. {@code false} otherwise.
   */
  public boolean collides(final double x, final double y) {
    return this.collides(new Point2D.Double(x, y));
  }

  /**
   * Checks if the point at the given coordinates collides with anything of the given {@code Collision} type.
   *
   * @param x         The x coordinate to check for collision.
   * @param y         The y coordinate to check for collision.
   * @param collision The {@code Collision} type to check for collisions.
   * @return {@code true} if the coordinates collide with anything of the given {@code Collision} type. {@code false} otherwise.
   * @see Collision
   */
  public boolean collides(double x, double y, Collision collision) {
    return this.collides(new Point2D.Double(x, y), collision);
  }

  /**
   * Checks if a given {@code ICollisionEntity} collides with anything that intersects specific coordinates.
   *
   * @param x      The x coordinate to check for collision.
   * @param y      The y coordinate to check for collision.
   * @param entity The {@code ICollisionEntity} to check for collision.
   * @return {@code true} if the entity collides with any {@code ICollisionEntity} on the given coordinates. {@code false} otherwise.
   * @see ICollisionEntity
   */
  public boolean collides(double x, double y, ICollisionEntity entity) {
    return collides(new Point2D.Double(x, y), entity);
  }

  /**
   * Checks if a given {@code ICollisionEntity} collides with anything registered in the {@code PhysicsEngine}.
   *
   * @param entity The {@code ICollisionEntity} to check for collision.
   * @return {@code true} if the entity collides with any other {@code ICollisionEntity}. {@code false} otherwise.
   * @see ICollisionEntity
   */
  public boolean collides(ICollisionEntity entity) {
    return this.collides(entity, Collision.ANY);
  }

  /**
   * Checks if a given {@code ICollisionEntity} collides with anything of the given {@code Collision} type.
   *
   * @param entity    The {@code ICollisionEntity} to check for collision.
   * @param collision The {@code Collision} type to check for collisions.
   * @return {@code true} if the entity collides with anything of the given {@code Collision} type. {@code false} otherwise.
   * @see Collision
   */
  public boolean collides(ICollisionEntity entity, Collision collision) {
    return this.collides(entity.getCollisionBox(), collision, entity);
  }

  /**
   * From a given point, cast a ray of indefinite length with the given angle and see if it hits anything.
   *
   * @param start The start point of the raycast.
   * @param angle The angle in degrees.
   * @return A {@code RaycastHit} determining the hit point, ray length, and corresponding {@code ICollisionEntity}, if the ray hit something.
   */
  public RaycastHit raycast(Point2D start, double angle) {
    double diameter = GeometricUtilities.getDiagonal(this.environmentBounds);
    return raycast(start, GeometricUtilities.project(start, angle, diameter));
  }

  /**
   * From a given point, cast a ray to another point and see if it hits anything.
   *
   * @param start  The start point of the raycast.
   * @param target The end point of the raycast.
   * @return A {@code RaycastHit} determining the hit point, ray length, and corresponding {@code ICollisionEntity}, if the ray hit something.
   */
  public RaycastHit raycast(Point2D start, Point2D target) {
    return raycast(start, target, Collision.ANY);
  }

  /**
   * From a given point, cast a ray to another point and see if it hits anything with the given {@code Collision} type.
   *
   * @param start     The start point of the raycast.
   * @param target    The end point of the raycast.
   * @param collision The {@code Collision} type to check for collision.
   * @return A {@code RaycastHit} determining the hit point, ray length, and corresponding {@code ICollisionEntity}, if the ray hit something.
   */
  public RaycastHit raycast(Point2D start, Point2D target, Collision collision) {
    final Line2D line = new Line2D.Double(start.getX(), start.getY(), target.getX(), target.getY());
    return raycast(line, collision, null);
  }

  /**
   * Cast a ray along a given line [from (x1,y1) to (x2,y2)] and see if it hits anything.
   *
   * @param line The line along which the ray is cast.
   * @return A {@code RaycastHit} determining the hit point, ray length, and corresponding {@code ICollisionEntity}, if the ray hit something.
   */
  public RaycastHit raycast(Line2D line) {
    return raycast(line, Collision.ANY, null);
  }

  /**
   * Cast a ray along a given line [from (x1,y1) to (x2,y2)] and see if it hits anything with the given {@code Collision} type.
   *
   * @param line      The line along which the ray is cast.
   * @param collision The {@code Collision} type to check for collision.
   * @return A {@code RaycastHit} determining the hit point, ray length, and corresponding {@code ICollisionEntity}, if the ray hit something.
   */
  public RaycastHit raycast(Line2D line, Collision collision) {
    return raycast(line, collision, null);
  }

  /**
   * Cast a ray along a given line [from (x1,y1) to (x2,y2)] and see if it hits a given {@code ICollisionEntity}.
   *
   * @param line   The line along which the ray is cast.
   * @param entity The {@code ICollisionEntity} type to check for collision.
   * @return A {@code RaycastHit} determining the hit point, ray length, and corresponding {@code ICollisionEntity}.
   */
  public RaycastHit raycast(Line2D line, ICollisionEntity entity) {
    return raycast(line, Collision.ANY, entity);
  }

  /**
   * Cast a ray along a given line [from (x1,y1) to (x2,y2)] and see if it hits anything with a certain {@code Collision} type that collides with the
   * given {@code ICollisionEntity}.
   *
   * @param line      The line along which the ray is cast.
   * @param collision The {@code Collision} type to check for collision.
   * @param entity    The {@code ICollisionEntity} type to check for collision.
   * @return A {@code RaycastHit} determining the hit point, ray length, and corresponding {@code ICollisionEntity}.
   */
  public RaycastHit raycast(Line2D line, Collision collision, ICollisionEntity entity) {
    final Point2D rayCastSource = new Point2D.Double(line.getX1(), line.getY1());

    for (final ICollisionEntity collisionEntity : this.collisionEntities.get(collision)) {
      if (!canCollide(entity, collisionEntity)) {
        continue;
      }

      if (collisionEntity.getCollisionBox().intersectsLine(line)) {
        double closestDist = -1;
        Point2D closestPoint = null;
        for (final Point2D intersection : GeometricUtilities.getIntersectionPoints(line, collisionEntity.getCollisionBox())) {
          final double dist = intersection.distance(rayCastSource);
          if (closestPoint == null || dist < closestDist) {
            closestPoint = intersection;
            closestDist = dist;
          }
        }

        return new RaycastHit(closestPoint, collisionEntity, closestDist);
      }
    }

    return null;
  }

  /**
   * Moves the specified entity by a given distance and angle.
   *
   * @param entity   The entity which is moved
   * @param angle    The angle in degrees
   * @param distance The distance to move the entity
   * @return {@code true}, if the entity can be moved without colliding, otherwise {@code false}.
   * @see GeometricUtilities#project(Point2D, double, double)
   */
  public boolean move(final IMobileEntity entity, final double angle, final double distance) {
    final Point2D newPosition = GeometricUtilities.project(entity.getLocation(), angle, distance);
    return this.move(entity, newPosition);
  }

  /**
   * Moves the specified entity by a given distance and angle.
   *
   * @param entity    The {@code IMobileEntity} which is moved
   * @param direction The {@code Direction} in which the entity is moved
   * @param distance  The distance to move the entity
   * @return {@code true}, if the entity can be moved without colliding, otherwise {@code false}.
   * @see Direction
   */
  public boolean move(IMobileEntity entity, Direction direction, double distance) {
    return this.move(entity, direction.toAngle(), distance);
  }

  /**
   * Moves the specified entity by a given distance towards the target coordinates.
   *
   * @param entity   The {@code IMobileEntity} which is moved
   * @param x        The target x coordinate
   * @param y        The target y coordinate
   * @param distance The distance to move the entity
   * @return {@code true}, if the entity can be moved without colliding, otherwise {@code false}.
   */
  public boolean move(final IMobileEntity entity, final double x, final double y, final float distance) {
    return this.move(entity, new Point2D.Double(x, y), distance);
  }

  /**
   * Moves the specified entity by a given distance and the entity's angle.
   *
   * @param entity   The {@code IMobileEntity} which is moved
   * @param distance The distance to move the entity
   * @return {@code true}, if the entity can be moved without colliding, otherwise {@code false}.
   * @see Direction
   */
  public boolean move(final IMobileEntity entity, final float distance) {
    return this.move(entity, entity.getAngle(), distance);
  }

  /**
   * Moves the specified entity to a target point. If {@code entity.turnOnMove()} is {@code true}, set the entity's angle towards the target.
   *
   * @param entity The {@code IMobileEntity} which is moved
   * @param target The target point
   * @return {@code true}, if the entity can be moved without colliding, otherwise {@code false}.
   * @see #resolveCollisionForNewPosition
   */
  public boolean move(final IMobileEntity entity, Point2D target) {
    if (entity.turnOnMove()) {
      entity.setAngle((float) GeometricUtilities.calcRotationAngleInDegrees(entity.getLocation(), target));
    }

    // don't set new location if it is outside the boundaries of the map
    if (!this.isInMap(entity.getCollisionBox(target))) {
      target = this.clamptoMap(entity, target);
    }

    if (!entity.hasCollision()) {
      entity.setLocation(target);
      return true;
    }

    // check if there is any collision to resolve on the new location
    if (this.resolveCollisionForNewPosition(entity, target)) {
      return false;
    }

    // This method provides a simplified approach for a multi-sampling algorithm
    // to prevent glitching through collision boxes that are smaller than the
    // movement step size
    if (this.resolveCollisionForRaycastToNewPosition(entity, target)) {
      return false;
    }

    // set new map location
    entity.setLocation(target);
    return true;
  }

  /**
   * Moves the specified entity by a given distance towards the target coordinates. If {@code entity.turnOnMove()} is {@code true}, set the entity's
   * angle towards the target.
   *
   * @param entity   The {@code IMobileEntity} which is moved
   * @param target   The target point
   * @param distance The distance to move the entity
   * @return {@code true}, if the entity can be moved without colliding, otherwise {@code false}.
   * @see #resolveCollisionForNewPosition
   */
  public boolean move(final IMobileEntity entity, final Point2D target, final float distance) {
    final Point2D newPosition = GeometricUtilities.project(entity.getLocation(), target, distance);
    return this.move(entity, newPosition);
  }

  /**
   * Clears all collision boxes registered on the {@code PhysicsEngine} once per tick and re-adds them with their updated positions.
   */
  @Override
  public void update() {
    // retrieve all collision box rectangles once per update
    for (Collision type : Collision.values()) {
      if (type == Collision.NONE) {
        continue;
      }

      this.collisionBoxes.get(type).clear();
      this.collisionBoxes.get(type)
          .addAll(this.collisionEntities.get(type).stream().map(ICollisionEntity::getCollisionBox).collect(Collectors.toList()));
    }
  }

  /**
   * Checks if two entities can collide
   *
   * @param entity      The first entity to check for collision
   * @param otherEntity The second entity to check for collision
   * @return {@code true} if the entities can collide, {@code false} otherwise.
   */
  private static boolean canCollide(ICollisionEntity entity, ICollisionEntity otherEntity) {
    if (otherEntity == null || !otherEntity.hasCollision()) {
      return false;
    }

    // the entity to check against is not provided
    if (entity == null) {
      return true;
    }

    // cannot collide with itself
    if (otherEntity.equals(entity)) {
      return false;
    }

    // an entity cannot collide with other entities that are excluded from collision by the canCollideWith method
    return entity.canCollideWith(otherEntity);
  }

  /**
   * Gets the intersection between an entity's collision box and all {@code ICollisionEntities} in a given rectangle.
   *
   * @param entity The {@code ICollisionEntity} to check for intersection.
   * @param rect   The {@code Rectangle2D} to check for intersection.
   * @return The {@code Intersection} area.
   */
  private Intersection getIntersection(final ICollisionEntity entity, final Rectangle2D rect) {
    Intersection result = null;
    for (final ICollisionEntity otherEntity : this.getCollisionEntities()) {
      if (!canCollide(entity, otherEntity)) {
        continue;
      }

      if (GeometricUtilities.intersects(otherEntity.getCollisionBox(), rect)) {
        Rectangle2D intersection = otherEntity.getCollisionBox().createIntersection(rect);
        if (result != null) {
          result = new Intersection(intersection.createUnion(result), ArrayUtilities.append(result.involvedEntities, otherEntity));
        } else {
          result = new Intersection(intersection, otherEntity);
        }
      }
    }

    return result;
  }

  private boolean collides(final ICollisionEntity entity, Collision type, Predicate<ICollisionEntity> check) {
    for (final ICollisionEntity otherEntity : this.getCollisionEntities(type)) {
      if (!canCollide(entity, otherEntity)) {
        continue;
      }

      if (check.test(otherEntity)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks if is in map.
   *
   * @param collisionBox the collision box
   * @return true, if is in map
   */
  private boolean isInMap(final Shape collisionBox) {
    if (this.environmentBounds == null) {
      return true;
    }

    return this.environmentBounds.contains(collisionBox.getBounds());
  }

  /**
   * With the current physics implementation is is possible to glitch through
   * other entities, if their collisionbox is smaller than the velocity of the
   * moving entity and they also move towards the currently moving entity.
   *
   * @param entity         The entity to resolve the collision for.
   * @param targetPosition The target position to which the entity should be moved to.
   * @return The position to which the entity should be moved after resolving the collision.
   */
  private Point2D resolveCollision(final ICollisionEntity entity, final Point2D targetPosition) {
    // first resolve x-axis movement
    Point2D resolvedPosition = new Point2D.Double(targetPosition.getX(), entity.getY());

    final Rectangle2D targetCollisionBoxX = entity.getCollisionBox(resolvedPosition);
    final Intersection intersectionX = this.getIntersection(entity, targetCollisionBoxX);
    if (intersectionX != null) {
      if (entity.getCollisionBox().getX() < targetCollisionBoxX.getX()) {
        // entity was moved left -> right, so push out to the left
        resolvedPosition.setLocation(Math.max(entity.getX(), resolvedPosition.getX() - intersectionX.getWidth()), resolvedPosition.getY());
      } else {
        // push it out to the right
        resolvedPosition.setLocation(Math.min(entity.getX(), resolvedPosition.getX() + intersectionX.getWidth()), resolvedPosition.getY());
      }
    }

    // then resolve y-axis movement
    resolvedPosition.setLocation(resolvedPosition.getX(), targetPosition.getY());

    final Rectangle2D targetCollisionBoxY = entity.getCollisionBox(resolvedPosition);
    final Intersection intersectionY = this.getIntersection(entity, targetCollisionBoxY);
    if (intersectionY != null) {
      if (entity.getCollisionBox().getY() < targetCollisionBoxY.getY()) {
        // entity was moved top -> bottom so push out towards the top
        resolvedPosition.setLocation(resolvedPosition.getX(), Math.max(entity.getY(), resolvedPosition.getY() - intersectionY.getHeight()));
      } else {
        resolvedPosition.setLocation(resolvedPosition.getX(), Math.min(entity.getY(), resolvedPosition.getY() + intersectionY.getHeight()));
      }
    }

    fireCollisionEvents(entity, intersectionX, intersectionY);

    return resolvedPosition;
  }

  private Point2D clamptoMap(IMobileEntity entity, Point2D newLocation) {
    double collisionLocationX = entity.getCollisionBoxAlign().getLocation(entity.getWidth(), entity.getCollisionBoxWidth());
    double leftBoundX = this.getBounds().getMinX() - collisionLocationX;
    double deltaX = entity.getWidth() - entity.getCollisionBoxWidth() - collisionLocationX;
    double rightBoundX = this.getBounds().getMaxX() - entity.getWidth() + deltaX;

    double collisionLocationY = entity.getCollisionBoxValign().getLocation(entity.getHeight(), entity.getCollisionBoxHeight());
    double topBoundY = this.getBounds().getMinY() - collisionLocationY;
    double deltaY = entity.getHeight() - entity.getCollisionBoxHeight() - collisionLocationY;
    double buttomBoundY = this.getBounds().getMaxY() - entity.getHeight() + deltaY;

    // right and left border minus the collision box width
    double x = MathUtilities.clamp(newLocation.getX(), leftBoundX, rightBoundX);
    // bottom and top border minus the collision box height
    double y = MathUtilities.clamp(newLocation.getY(), topBoundY, buttomBoundY);
    return new Point2D.Double(x, y);
  }

  private boolean resolveCollisionForNewPosition(ICollisionEntity entity, Point2D location) {
    // resolve collision for new location
    if (this.collides(entity.getCollisionBox(location), entity)) {
      final Point2D resolvedPosition = this.resolveCollision(entity, location);
      entity.setLocation(resolvedPosition);
      return true;
    }

    return false;
  }

  private boolean resolveCollisionForRaycastToNewPosition(ICollisionEntity entity, Point2D newPosition) {
    // special case to prevent entities to glitch through collision boxes if
    // they have a large enough step size
    final Line2D line = new Line2D.Double(entity.getCollisionBox().getCenterX(), entity.getCollisionBox().getCenterY(),
        entity.getCollisionBox(newPosition).getCenterX(), entity.getCollisionBox(newPosition).getCenterY());
    return this.collides(line, Collision.ANY, entity);
  }

  private static void fireCollisionEvents(ICollisionEntity collider, Intersection... intersections) {
    // aggregate the involved entities of all intersections
    ICollisionEntity[] involvedEntities = null;
    for (Intersection inter : intersections) {
      if (inter == null) {
        continue;
      }

      if (involvedEntities == null) {
        involvedEntities = inter.involvedEntities;
        continue;
      }

      involvedEntities = ArrayUtilities.distinct(involvedEntities, inter.involvedEntities);
    }

    // 1. fire collision event on the collider with all the involved entities
    CollisionEvent event = new CollisionEvent(collider, involvedEntities);
    collider.fireCollisionEvent(event);

    // 2. fire collision event on the involved entities with the collider entity
    CollisionEvent colliderEvent = new CollisionEvent(collider);
    for (ICollisionEntity involved : involvedEntities) {
      involved.fireCollisionEvent(colliderEvent);
    }
  }

  /**
   * A helper class that contains the intersection of a collision event and the involved entities.
   * This is basically just a {@link Rectangle2D} with some additional information.
   */
  private static class Intersection extends Rectangle2D.Double {
    private final transient ICollisionEntity[] involvedEntities;

    public Intersection(Rectangle2D rect, ICollisionEntity... entities) {
      super(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
      this.involvedEntities = entities;
    }
  }
}