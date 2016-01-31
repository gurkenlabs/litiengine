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

  /**
   * Checks if is in map.
   *
   * @param collisionBox
   *          the collision box
   * @return true, if is in map
   */
  private boolean isInMap(final Shape collisionBox) {
    return this.environmentBounds.contains(collisionBox.getBounds());
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.physics.IPhysicsEngine#move(de.gurkenlabs.liti.entities.
   * Entity, int, float)
   */
  /*
   * Moves the given entity in the specified angle by the specified delta.
   *
   * @see
   * de.gurkenlabs.liti.physics.IPhysicsEngine#move(de.gurkenlabs.liti.entities
   * * .Entity, int, double)
   */
  @Override
  public boolean move(final IMovableEntity entity, final double angle, final float delta) {

    Point2D newPosition = GeometricUtilities.project(entity.getLocation(), angle, delta);
    boolean success = true;

    if (entity.hasCollision() && this.collidesWithAnyEntity(entity, newPosition) || this.collidesWithAnyStaticCollisionBox(entity, newPosition)) {
      newPosition = this.findLocationWithoutCollision(entity, newPosition);
      success = false;
    }

    // don't set new location if it is outside the boundaries of the map
    if (!isInMap(entity.getCollisionBox(newPosition))) {
      return false;
    }

    // set new map location
    entity.setLocation(newPosition);
    entity.setFacingAngle(Math.round(angle));

    return success;
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
      if (otherEntity.equals(entity)) {
        continue;
      }

      if (!otherEntity.hasCollision()) {
        continue;
      }

      if (otherEntity.getCollisionBox().intersects(entity.getCollisionBox(newPosition))) {
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
    for (final Rectangle2D shape : this.staticCollisionBoxes) {
      if (shape.intersects(entity.getCollisionBox(newPosition))) {
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

  @Override
  public void add(ICollisionEntity entity) {
    if (!this.collisionEntities.contains(entity)) {
      this.collisionEntities.add(entity);
    }
  }

  @Override
  public void remove(ICollisionEntity entity) {
    if (this.collisionEntities.contains(entity)) {
      this.collisionEntities.remove(entity);
    }
  }

  @Override
  public void add(Rectangle2D staticCollisionBox) {
    if (!this.staticCollisionBoxes.contains(staticCollisionBox)) {
      this.staticCollisionBoxes.add(staticCollisionBox);
    }
  }

  @Override
  public void remove(Rectangle2D staticCollisionBox) {
    if (this.staticCollisionBoxes.contains(staticCollisionBox)) {
      this.staticCollisionBoxes.remove(staticCollisionBox);
    }
  }

  @Override
  public void clear() {
    this.staticCollisionBoxes.clear();
    this.collisionEntities.clear();
  }

  @Override
  public void setBounds(Rectangle2D environmentBounds) {
    this.environmentBounds = environmentBounds;
  }

  @Override
  public List<Rectangle2D> getAllCollisionBoxes() {
    final List<Rectangle2D> allCollisionBoxes = new ArrayList<>();
    allCollisionBoxes.addAll(this.collisionEntities.stream().filter(x -> x.hasCollision()).map(x -> x.getCollisionBox()).collect(Collectors.toList()));
    allCollisionBoxes.addAll(this.staticCollisionBoxes);

    return allCollisionBoxes;
  }
}
