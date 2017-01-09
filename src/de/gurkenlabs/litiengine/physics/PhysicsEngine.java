/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.physics;

import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.util.geom.GeometricUtilities;

/**
 * The Class PhysicsEngine.
 */
public class PhysicsEngine implements IPhysicsEngine {
  private final List<ICollisionEntity> collisionEntities;

  private final List<Rectangle2D> staticCollisionBoxes;

  private final List<Rectangle2D> allCollisionBoxes;

  private Rectangle2D environmentBounds;

  public final double PHYSICS_MARGIN = 0.5;

  /**
   * Instantiates a new physics engine.
   */
  public PhysicsEngine() {
    this.collisionEntities = new CopyOnWriteArrayList<>();
    this.staticCollisionBoxes = new CopyOnWriteArrayList<>();
    this.allCollisionBoxes = new CopyOnWriteArrayList<>();
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
      return this.collidesWithAnyStaticCollisionBox(null, rect) != null;
    }

    return false;
  }

  @Override
  public List<Rectangle2D> getAllCollisionBoxes() {
    return this.allCollisionBoxes;
  }

  @Override
  public List<Rectangle2D> getStaticCollisionBoxes() {
    return this.staticCollisionBoxes;
  }

  @Override
  public List<ICollisionEntity> getCollisionEntities() {
    return this.collisionEntities;
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
  public boolean move(final IMovableEntity entity, final float angle, final float delta) {
    final Point2D newPosition = GeometricUtilities.project(entity.getLocation(), angle, delta);
    return this.move(entity, newPosition);
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

    if (entity.hasCollision()) {
      final Rectangle2D entityCollisionBox = entity.getCollisionBox(newPosition);

      // resolve static collision boxes first
      Rectangle2D staticIntersection = this.collidesWithAnything(entity, entityCollisionBox);
      int cnt = 0;
      while (staticIntersection != null & cnt < 4) {
        newPosition = this.resolveCollision(entity, newPosition, staticIntersection);
        staticIntersection = this.collidesWithAnything(entity, entity.getCollisionBox(newPosition));
        success = false;
        cnt++;
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
    this.allCollisionBoxes.clear();
    this.allCollisionBoxes.addAll(this.collisionEntities.stream().filter(x -> x.hasCollision()).map(x -> x.getCollisionBox()).collect(Collectors.toList()));
    this.allCollisionBoxes.addAll(this.staticCollisionBoxes);
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
      if (otherEntity.equals(entity)) {
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
  private Rectangle2D collidesWithAnyStaticCollisionBox(final ICollisionEntity entity, final Rectangle2D entityCollisionBox) {
    for (final Rectangle2D collisionBox : this.staticCollisionBoxes) {
      if (GeometricUtilities.intersects(collisionBox, entityCollisionBox)) {
        return collisionBox.createIntersection(entityCollisionBox);
      }
    }

    return null;
  }

  private Rectangle2D collidesWithAnything(final ICollisionEntity entity, final Rectangle2D entityCollisionBox) {

    Rectangle2D newCollisionBoxWithMargin = new Rectangle2D.Double(entityCollisionBox.getX() - PHYSICS_MARGIN, entityCollisionBox.getY() - PHYSICS_MARGIN, entityCollisionBox.getWidth() + PHYSICS_MARGIN * 2, entityCollisionBox.getHeight() + PHYSICS_MARGIN * 2);
    for (final Rectangle2D collisionBox : this.getAllCollisionBoxes()) {
      if (collisionBox.equals(entity.getCollisionBox())) {
        continue;
      }

      if (GeometricUtilities.intersects(collisionBox, newCollisionBoxWithMargin)) {
        return collisionBox.createIntersection(newCollisionBoxWithMargin);
      }
    }

    return null;
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

  private Point2D resolveCollision(final IMovableEntity entity, final Point2D newPosition, final Rectangle2D intersection) {

    final Point2D resolvedPosition = newPosition;
    final double smallerDistance = Math.min(intersection.getWidth(), intersection.getHeight());
    if (smallerDistance == intersection.getWidth()) {
      // x axis resolving
      if (Math.abs(newPosition.getX() - entity.getLocation().getX()) < PHYSICS_MARGIN) {
        resolvedPosition.setLocation(entity.getLocation().getX(), newPosition.getY());
      } else if (entity.getCollisionBox().getX() < intersection.getMaxX()) {
        // new position is closer to the left side, so push out to the left
        resolvedPosition.setLocation(resolvedPosition.getX() - (Math.max(PHYSICS_MARGIN, intersection.getWidth() + PHYSICS_MARGIN)), newPosition.getY());
      } else {
        // push it out to the right
        resolvedPosition.setLocation(resolvedPosition.getX() + Math.max(PHYSICS_MARGIN, intersection.getWidth() + PHYSICS_MARGIN), newPosition.getY());
      }
    } else {
      // y axis resolving
      if (Math.abs(newPosition.getY() - entity.getLocation().getY()) < PHYSICS_MARGIN) {
        resolvedPosition.setLocation(newPosition.getX(), entity.getLocation().getY());
      } else if (entity.getCollisionBox().getY() < intersection.getMaxY()) {
        // new position is closer to the top
        resolvedPosition.setLocation(newPosition.getX(), resolvedPosition.getY() - Math.max(PHYSICS_MARGIN, intersection.getHeight() + PHYSICS_MARGIN));
      } else {
        resolvedPosition.setLocation(newPosition.getX(), resolvedPosition.getY() + Math.max(PHYSICS_MARGIN, intersection.getHeight() + PHYSICS_MARGIN));
      }
    }

    return resolvedPosition;
  }
}