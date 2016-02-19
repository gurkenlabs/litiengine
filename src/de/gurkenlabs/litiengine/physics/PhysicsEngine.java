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

  @Override
  public boolean move(IMovableEntity entity, Point2D target, float delta) {
    Point2D newPosition = GeometricUtilities.project(entity.getLocation(), target, delta);
    return this.move(entity, newPosition);
  }

  @Override
  public boolean move(final IMovableEntity entity, final float angle, final float delta) {
    Point2D newPosition = GeometricUtilities.project(entity.getLocation(), angle, delta);
    return this.move(entity, newPosition);
  }

  private boolean move(final IMovableEntity entity, Point2D newPosition) {
    boolean success = true;
    entity.setFacingAngle((float) GeometricUtilities.calcRotationAngleInDegrees(entity.getLocation(), newPosition));

    if (entity.hasCollision() && this.collidesWithAnyEntity(entity, newPosition) || this.collidesWithAnyStaticCollisionBox(entity, newPosition)) {
      newPosition = this.findLocationWithoutCollision(entity, newPosition);
      success = false;
    }

    // don't set new location if it is outside the boundaries of the map
    if (!this.isInMap(entity.getCollisionBox(newPosition))) {
      return false;
    }

    // set new map location
    entity.setLocation(newPosition);
    return success;
  }

  @Override
  public boolean move(final IMovableEntity entity, final float delta) {
    return this.move(entity, entity.getFacingAngle(), delta);
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

  /**
   * Collides with any entity.
   *
   * @param entity
   *          the entity
   * @param newPosition
   *          the new position
   * @return true, if successful
   */
  private boolean collidesWithAnyEntity(final ICollisionEntity entity, final Point2D newPosition) {
    for (final ICollisionEntity otherEntity : this.collisionEntities) {
      if (!otherEntity.hasCollision()) {
        continue;
      }

      if (otherEntity.equals(entity)) {
        continue;
      }

      if (collides(otherEntity.getCollisionBox(), entity.getCollisionBox(newPosition))) {
        return true;
      }
    }

    return false;
  }

  private static boolean collides(Rectangle2D a, Rectangle2D b) {
    if (Math.abs(a.getCenterX() - b.getCenterX()) < a.getWidth() / 2 + b.getWidth() / 2) {
      if (Math.abs(a.getCenterY() - b.getCenterY()) < a.getHeight() / 2 + b.getHeight() / 2) {
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
  private boolean collidesWithAnyStaticCollisionBox(final ICollisionEntity entity, final Point2D newPosition) {
    for (final Rectangle2D collisionBox : this.staticCollisionBoxes) {
      if (collides(collisionBox, entity.getCollisionBox(newPosition))) {
        return true;
      }
    }

    return false;
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
      if (!this.collidesWithAnyEntity(mob, pointBetween) && !this.collidesWithAnyStaticCollisionBox(mob, pointBetween)) {
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
}
