/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.physics;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.util.geom.GeometricUtilities;

/**
 * The Class PhysicsEngine.
 */
public class PhysicsEngine implements IPhysicsEngine {
  private final List<ICollisionEntity> collisionEntities;

  private final List<Rectangle2D> staticCollisionBoxes;

  private Rectangle2D environmentBounds;

  private boolean turnEntityOnMove = true;

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
  public List<Rectangle2D> getAllCollisionBoxes() {
    final List<Rectangle2D> allCollisionBoxes = new ArrayList<>();
    allCollisionBoxes.addAll(this.collisionEntities.stream().filter(x -> x.hasCollision()).map(x -> x.getCollisionBox()).collect(Collectors.toList()));
    allCollisionBoxes.addAll(this.staticCollisionBoxes);

    return allCollisionBoxes;
  }

  public List<Rectangle2D> getAllCollisionBoxes(ICollisionEntity entity) {
    final List<Rectangle2D> allCollisionBoxes = new ArrayList<>();
    allCollisionBoxes.addAll(this.collisionEntities.stream().filter(x -> x.hasCollision() && entity.collidesWith(x)).map(x -> x.getCollisionBox()).collect(Collectors.toList()));
    allCollisionBoxes.addAll(this.staticCollisionBoxes);

    return allCollisionBoxes;
  }

  @Override
  public boolean move(final IMovableEntity entity, final Point2D target, final float delta) {
    final Point2D newPosition = GeometricUtilities.project(entity.getLocation(), target, delta);
    return this.move(entity, newPosition);
  }

  @Override
  public boolean move(final IMovableEntity entity, final float angle, final float delta) {
    final Point2D newPosition = GeometricUtilities.project(entity.getLocation(), angle, delta);
    return this.move(entity, newPosition);
  }

  @Override
  public boolean move(final IMovableEntity entity, final float delta) {
    return this.move(entity, entity.getAngle(), delta);
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
      if (collides(rect, collisionBox)) {
        return true;
      }
    }

    return false;
  }

  public boolean collides(ICollisionEntity entity, final Rectangle2D rect) {
    for (final Rectangle2D collisionBox : this.getAllCollisionBoxes(entity)) {
      if (collides(rect, collisionBox)) {
        return true;
      }
    }

    return false;
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
  private Rectangle2D collidesWithAnyEntity(final ICollisionEntity entity, final Point2D newPosition) {
    for (final ICollisionEntity otherEntity : this.collisionEntities) {
      if (!otherEntity.hasCollision()) {
        continue;
      }

      if (otherEntity.equals(entity)) {
        continue;
      }

      if (collides(otherEntity.getCollisionBox(), entity.getCollisionBox(newPosition))) {
        return otherEntity.getCollisionBox().createIntersection(entity.getCollisionBox(newPosition));
      }
    }

    return null;
  }

  private static boolean collides(final Rectangle2D a, final Rectangle2D b) {
    if (Math.abs(a.getCenterX() - b.getCenterX()) < a.getWidth() * 0.5 + b.getWidth() * 0.5) {
      if (Math.abs(a.getCenterY() - b.getCenterY()) < a.getHeight() * 0.5 + b.getHeight() * 0.5) {
        return true;
      }
    }

    return false;
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
  private Rectangle2D collidesWithAnyStaticCollisionBox(final ICollisionEntity entity, final Point2D newPosition) {
    for (final Rectangle2D collisionBox : this.staticCollisionBoxes) {
      if (collides(collisionBox, entity.getCollisionBox(newPosition))) {
        return collisionBox.createIntersection(entity.getCollisionBox(newPosition));
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
      if (this.collidesWithAnyEntity(mob, pointBetween) == null && this.collidesWithAnyStaticCollisionBox(mob, pointBetween) == null) {
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

  private boolean move(final IMovableEntity entity, Point2D newPosition) {
    boolean success = true;
    if (this.turnEntityOnMove) {
      entity.setAngle((float) GeometricUtilities.calcRotationAngleInDegrees(entity.getLocation(), newPosition));
    }
    
    if (entity.hasCollision()) {
      final Rectangle2D staticIntersection = this.collidesWithAnyStaticCollisionBox(entity, newPosition);
      if (staticIntersection != null) {
        newPosition = this.resolveCollision(entity, newPosition, staticIntersection);
        success = false;
      }

      final Rectangle2D entityIntersection = this.collidesWithAnyEntity(entity, newPosition);
      if (entityIntersection != null) {
        newPosition = this.resolveCollision(entity, newPosition, entityIntersection);
        success = false;
      }
    }

    // don't set new location if it is outside the boundaries of the map
    if (!this.isInMap(entity.getCollisionBox(newPosition))) {
      return false;
    }

    // set new map location
    entity.setLocation(newPosition);
    return success;
  }

  private Point2D resolveCollision(final IMovableEntity entity, final Point2D newPosition, final Rectangle2D intersection) {
    final Point2D resolvedPosition = newPosition;
    final double smallerDistance = Math.min(intersection.getWidth(), intersection.getHeight());
    if (smallerDistance == intersection.getWidth()) {
      // x axis resolving
      if (Math.abs(newPosition.getX() - intersection.getMinX()) < Math.abs(newPosition.getX() - intersection.getMaxX())) {
        // new position is closer to the left side, so push out to the left
        resolvedPosition.setLocation(resolvedPosition.getX() - intersection.getWidth(), resolvedPosition.getY());
      } else {
        // push it out to the right
        resolvedPosition.setLocation(resolvedPosition.getX() + intersection.getWidth(), resolvedPosition.getY());
      }
    } else {
      // y axis resolving
      if (Math.abs(newPosition.getY() - intersection.getMinY()) < Math.abs(newPosition.getY() - intersection.getMaxY())) {
        // new position is closer to the top
        resolvedPosition.setLocation(resolvedPosition.getX(), resolvedPosition.getY() - intersection.getHeight());
      } else {
        resolvedPosition.setLocation(resolvedPosition.getX(), resolvedPosition.getY() + intersection.getHeight());
      }
    }

    // if the resolved location still collides with something
    if (this.collides(entity.getCollisionBox(resolvedPosition))) {
      // fallback
      return this.findLocationWithoutCollision(entity, newPosition);
    }

    return resolvedPosition;
  }

  @Override
  public List<Rectangle2D> getStaticCollisionBoxes() {
    return this.staticCollisionBoxes;
  }

  @Override
  public boolean setTurnEntityOnMove(boolean turn) {
    return this.turnEntityOnMove = turn;
  }

  public boolean isTurnEntityOnMove() {
    return turnEntityOnMove;
  }
}