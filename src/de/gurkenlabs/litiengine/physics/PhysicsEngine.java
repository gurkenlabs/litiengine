/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.physics;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.util.geom.GeometricUtilities;

/**
 * The Class PhysicsEngine.
 */
public final class PhysicsEngine implements IPhysicsEngine {
  private final List<ICollisionEntity> collisionEntities;

  private Rectangle2D environmentBounds;

  private final List<Rectangle2D> staticCollisionBoxes;

  /**
   * Instantiates a new physics engine.
   */
  public PhysicsEngine() {
    this.collisionEntities = new CopyOnWriteArrayList<>();
    this.staticCollisionBoxes = new CopyOnWriteArrayList<>();
  }

  @Override
  public void add(final ICollisionEntity entity) {
    if (!this.collisionEntities.contains(entity)) {
      this.collisionEntities.add(entity);
    }
  }

  @Override
  public void add(final Rectangle2D staticCollisionBox) {
    if (!this.staticCollisionBoxes.contains(staticCollisionBox)) {
      this.staticCollisionBoxes.add(staticCollisionBox);
    }
  }

  @Override
  public void clear() {
    this.staticCollisionBoxes.clear();
    this.collisionEntities.clear();
  }

  @Override
  public boolean collides(final double x, final double y) {
    return this.collides(new Point2D.Double(x, y));
  }

  @Override
  public Point2D collides(final Line2D rayCast) {
    final Point2D rayCastSource = new Point2D.Double(rayCast.getX1(), rayCast.getY1());
    final List<Rectangle2D> collBoxes = this.getAllCollisionBoxes();
    collBoxes.sort((rect1, rect2) -> {
      final Point2D rect1Center = new Point2D.Double(rect1.getCenterX(), rect1.getCenterY());
      final Point2D rect2Center = new Point2D.Double(rect2.getCenterX(), rect2.getCenterY());
      final double dist1 = rect1Center.distance(rayCastSource);
      final double dist2 = rect2Center.distance(rayCastSource);

      if (dist1 < dist2) {
        return -1;
      }

      if (dist1 > dist2) {
        return 1;
      }

      return 0;
    });

    for (final Rectangle2D collisionBox : collBoxes) {
      if (collisionBox.intersectsLine(rayCast)) {
        double closestDist = -1;
        Point2D closestPoint = null;
        for (final Point2D intersection : GeometricUtilities.getIntersectionPoints(rayCast, collisionBox)) {
          final double dist = intersection.distance(rayCastSource);
          if (closestPoint == null || dist < closestDist) {
            closestPoint = intersection;
            closestDist = dist;
          }
        }

        return closestPoint;
      }
    }

    return null;
  }

  @Override
  public boolean collides(final Point2D point) {
    for (final Rectangle2D collisionBox : this.getAllCollisionBoxes()) {
      if (collisionBox.contains(point)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean collides(final Rectangle2D rect) {
    for (final Rectangle2D collisionBox : this.getAllCollisionBoxes()) {
      if (GeometricUtilities.intersects(rect, collisionBox)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean collides(final Rectangle2D rect, final int collisionType) {
    if ((collisionType & COLLTYPE_ALL) == COLLTYPE_ALL) {
      return this.collides(rect);
    }

    if ((collisionType & COLLTYPE_ENTITY) == COLLTYPE_ENTITY) {
      return this.collidesWithAnyEntity(null, rect) != null;
    }

    if ((collisionType & COLLTYPE_STATIC) == COLLTYPE_STATIC) {
      return this.collidesWithAnyStaticCollisionBox(rect) != null;
    }

    return false;
  }

  @Override
  public List<ICollisionEntity> collidesWithEntites(final Rectangle2D rect) {
    final List<ICollisionEntity> collEntities = new CopyOnWriteArrayList<>();
    for (final ICollisionEntity coll : Game.getPhysicsEngine().getCollisionEntities()) {
      if (coll.getCollisionBox().intersects(rect)) {
        collEntities.add(coll);
      }
    }

    return collEntities;
  }

  @Override
  public List<Rectangle2D> getAllCollisionBoxes() {
    return this.getCollisionBoxes().stream().map(s -> s.getCollisionBox()).collect(Collectors.toList());
  }

  private List<CollisionBox> getCollisionBoxes() {
    List<CollisionBox> allCollisionBoxes = new ArrayList<>();
    allCollisionBoxes.clear();
    allCollisionBoxes.addAll(this.collisionEntities.stream().filter(x -> x.hasCollision()).map(x -> new CollisionBox(x)).collect(Collectors.toList()));
    allCollisionBoxes.addAll(this.staticCollisionBoxes.stream().map(x -> new CollisionBox(x)).collect(Collectors.toList()));

    return allCollisionBoxes;
  }

  @Override
  public List<ICollisionEntity> getCollisionEntities() {
    return this.collisionEntities;
  }

  @Override
  public List<Rectangle2D> getStaticCollisionBoxes() {
    return this.staticCollisionBoxes;
  }

  @Override
  public boolean move(final IMovableEntity entity, final double angle, final double delta) {
    final Point2D newPosition = GeometricUtilities.project(entity.getLocation(), angle, delta);
    return this.move(entity, newPosition);
  }

  @Override
  public boolean move(final IMovableEntity entity, final double x, final double y, final float delta) {
    return this.move(entity, new Point2D.Double(x, y), delta);
  }

  @Override
  public boolean move(final IMovableEntity entity, final float delta) {
    return this.move(entity, entity.getAngle(), delta);
  }

  @Override
  public boolean move(final IMovableEntity entity, Point2D newPosition) {

    boolean success = true;
    if (entity.turnOnMove()) {
      entity.setAngle((float) GeometricUtilities.calcRotationAngleInDegrees(entity.getLocation(), newPosition));
    }

    // don't set new location if it is outside the boundaries of the map
    if (!this.isInMap(entity.getCollisionBox(newPosition))) {
      return false;
    }

    if (!entity.hasCollision()) {
      entity.setLocation(newPosition);
      return true;
    }

    // resolve collision for current location
    if (this.collidesWithAnything(entity, entity.getCollisionBox()) != null) {
      final Point2D resolvedPosition = this.resolveCollision(entity, entity.getLocation());
      entity.setLocation(resolvedPosition);
      success = false;
    }

    // resolve collision for new location;
    if (this.collidesWithAnything(entity, entity.getCollisionBox(newPosition)) != null) {
      final Point2D resolvedPosition = this.resolveCollision(entity, newPosition);
      entity.setLocation(resolvedPosition);
      return false;
    } else {

      // special case to prevent entities to glitch through collision boxes if
      // they have a large enough stepsize
      // TODO: this does not entirely fix the issue...
      final Line2D line = new Line2D.Double(entity.getCollisionBox().getCenterX(), entity.getCollisionBox().getCenterY(), entity.getCollisionBox(newPosition).getCenterX(), entity.getCollisionBox(newPosition).getCenterY());
      for (final CollisionBox collisionBox : this.getCollisionBoxes()) {
        if (collisionBox.getEntity() != null && (collisionBox.getEntity().equals(entity) || !entity.canCollideWith(collisionBox.getEntity()))) {
          continue;
        }

        // there was a collision inbetween
        final Point2D intersection = GeometricUtilities.getIntersectionPoint(line, collisionBox.getCollisionBox());
        if (intersection != null) {
          newPosition = entity.getLocation();
        }
      }
    }

    // set new map location
    entity.setLocation(newPosition);
    return success;
  }

  @Override
  public boolean move(final IMovableEntity entity, final Point2D target, final float delta) {
    final Point2D newPosition = GeometricUtilities.project(entity.getLocation(), target, delta);
    return this.move(entity, newPosition);
  }

  @Override
  public void remove(final ICollisionEntity entity) {
    if (this.collisionEntities.contains(entity)) {
      this.collisionEntities.remove(entity);
    }
  }

  @Override
  public void remove(final Rectangle2D staticCollisionBox) {
    if (this.staticCollisionBoxes.contains(staticCollisionBox)) {
      this.staticCollisionBoxes.remove(staticCollisionBox);
    }
  }

  @Override
  public void setBounds(final Rectangle2D environmentBounds) {
    this.environmentBounds = environmentBounds;
  }

  @Override
  public void update(final IGameLoop loop) {
  }

  /**
   * Collides with any entity.
   *
   * @param entity
   *          the entity
   * @param newPosition
   *          the new position
   * @return true, if successful
   */
  private Rectangle2D collidesWithAnyEntity(final ICollisionEntity entity, final Rectangle2D collisionBox) {
    for (final ICollisionEntity otherEntity : this.collisionEntities) {
      if (entity != null && (otherEntity.equals(entity) || !entity.canCollideWith(otherEntity))) {
        continue;
      }

      if (!otherEntity.hasCollision()) {
        continue;
      }

      if (GeometricUtilities.intersects(otherEntity.getCollisionBox(), collisionBox)) {
        return otherEntity.getCollisionBox().createIntersection(collisionBox);
      }
    }

    return null;
  }

  /**
   * Collides with any map object.
   *
   * @param entity
   *          the entity
   * @param newPosition
   *          the new position
   * @return true, if successful
   */
  private Rectangle2D collidesWithAnyStaticCollisionBox(final Rectangle2D entityCollisionBox) {
    for (final Rectangle2D collisionBox : this.staticCollisionBoxes) {
      if (GeometricUtilities.intersects(collisionBox, entityCollisionBox)) {
        return collisionBox.createIntersection(entityCollisionBox);
      }
    }

    return null;
  }

  private Rectangle2D collidesWithAnything(final ICollisionEntity entity, final Rectangle2D entityCollisionBox) {
    for (final CollisionBox collisionBox : this.getCollisionBoxes()) {

      // an entity cannot collide with itself or other entities that are
      // excluded from collision by the canCollideWith method
      if (collisionBox.getEntity() != null && (collisionBox.getEntity().equals(entity) || !entity.canCollideWith(collisionBox.getEntity()))) {
        continue;
      }

      if (collisionBox.getCollisionBox().contains(entityCollisionBox)) {
        return collisionBox.getCollisionBox();
      }

      if (GeometricUtilities.intersects(collisionBox.getCollisionBox(), entityCollisionBox)) {
        return collisionBox.getCollisionBox().createIntersection(entityCollisionBox);
      }
    }

    return null;
  }

  /**
   * Find location without collision.
   *
   * @param mob
   *          the mob
   * @param newPosition
   *          the new position
   * @return the point2 d
   */
  private Point2D findLocationWithoutCollision(final ICollisionEntity mob, final Point2D newPosition) {
    for (final Point2D pointBetween : GeometricUtilities.getPointsBetweenPoints(newPosition, mob.getLocation())) {
      final Rectangle2D newCollisionBox = mob.getCollisionBox(pointBetween);
      if (this.collidesWithAnyEntity(mob, newCollisionBox) == null && this.collidesWithAnyStaticCollisionBox(newCollisionBox) == null) {
        return pointBetween;
      }
    }

    return mob.getLocation();
  }

  /**
   * Checks if is in map.
   *
   * @param collisionBox
   *          the collision box
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
   * @param entity
   * @param newPosition
   * @return
   */
  private Point2D resolveCollision(final IMovableEntity entity, final Point2D newPosition) {
    // first resolve x-axis movement
    Point2D resolvedPosition = new Point2D.Double(newPosition.getX(), entity.getLocation().getY());

    // resolve static collision boxes first
    final Rectangle2D intersectionX = this.collidesWithAnything(entity, entity.getCollisionBox(resolvedPosition));
    if (intersectionX != null) {
      if (intersectionX.getWidth() > entity.getCollisionBox().getWidth() || intersectionX.getHeight() > entity.getCollisionBox().getHeight()) {
        resolvedPosition = this.findLocationWithoutCollision(entity, resolvedPosition);
      } else if (entity.getCollisionBox().getX() < intersectionX.getMaxX()) {
        // new position is closer to the left side, so push out to the left
        resolvedPosition.setLocation(Math.max(entity.getLocation().getX(), resolvedPosition.getX() - intersectionX.getWidth()), resolvedPosition.getY());
      } else {
        // push it out to the right
        resolvedPosition.setLocation(Math.min(entity.getLocation().getX(), resolvedPosition.getX() + intersectionX.getWidth()), resolvedPosition.getY());
      }
    }

    // then resolve y-axis movement
    resolvedPosition.setLocation(resolvedPosition.getX(), newPosition.getY());
    final Rectangle2D intersectionY = this.collidesWithAnything(entity, entity.getCollisionBox(resolvedPosition));

    if (intersectionY != null) {
      if (intersectionY.getWidth() > entity.getCollisionBox().getWidth() || intersectionY.getHeight() > entity.getCollisionBox().getHeight()) {
        resolvedPosition = this.findLocationWithoutCollision(entity, resolvedPosition);
      } else if (entity.getCollisionBox().getCenterY() - intersectionY.getCenterY() < 0) {
        // new position is closer to the top
        resolvedPosition.setLocation(resolvedPosition.getX(), Math.max(entity.getLocation().getY(), resolvedPosition.getY() - intersectionY.getHeight()));
      } else {
        resolvedPosition.setLocation(resolvedPosition.getX(), Math.min(entity.getLocation().getY(), resolvedPosition.getY() + intersectionY.getHeight()));
      }
    }

    return resolvedPosition;
  }

  private class CollisionBox {
    private final Rectangle2D box;

    private final ICollisionEntity entity;

    private CollisionBox(Rectangle2D box) {
      this.box = box;
      this.entity = null;
    }

    private CollisionBox(ICollisionEntity entity) {
      this.box = entity.getCollisionBox();
      this.entity = entity;
    }

    public Rectangle2D getCollisionBox() {
      return this.box;
    }

    public ICollisionEntity getEntity() {
      return this.entity;
    }
  }
}