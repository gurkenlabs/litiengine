package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.util.geom.GeometricUtilities;

public class MovementController<T extends IMovableEntity> implements IMovementController<T> {
  private final List<Force> activeForces;
  private final IPhysicsEngine engine;
  private final T movableEntity;
  private final List<Predicate<T>> movementPredicates;

  public MovementController(final IPhysicsEngine engine, final T movableEntity) {
    this.activeForces = new CopyOnWriteArrayList<>();
    this.movementPredicates = new CopyOnWriteArrayList<>();
    this.movableEntity = movableEntity;
    this.engine = engine;
  }

  @Override
  public void apply(final Force force) {
    if (!this.activeForces.contains(force)) {
      this.activeForces.add(force);
    }
  }

  @Override
  public List<Force> getActiceForces() {
    return this.activeForces;
  }

  @Override
  public T getEntity() {
    return this.movableEntity;
  }

  @Override
  public void onMovementCheck(final Predicate<T> predicate) {
    if (!this.movementPredicates.contains(predicate)) {
      this.movementPredicates.add(predicate);
    }
  }

  @Override
  public void update(final IGameLoop gameLoop) {
    this.handleForces(gameLoop);
  }

  protected IPhysicsEngine getPhysicsEngine() {
    return this.engine;
  }

  protected boolean isMovementAllowed() {
    for (final Predicate<T> predicate : this.movementPredicates) {
      if (!predicate.test(this.getEntity())) {
        return false;
      }
    }

    return true;
  }

  private void handleForces(final IGameLoop gameLoop) {
    final double ACCEPTABLE_DIST = 5;
    // clean up forces
    this.activeForces.forEach(x -> {
      if (x.hasEnded()) {
        this.activeForces.remove(x);
      }
    });

    // apply all forces
    // TODO: calculate the diff of all forces combined and only move the entity
    // once
    for (final Force force : this.activeForces) {
      if (force.cancelOnReached() && force.hasReached(this.getEntity())) {
        force.end();
        continue;
      }

      final Point2D collisionBoxCenter = new Point2D.Double(this.getEntity().getCollisionBox().getCenterX(), this.getEntity().getCollisionBox().getCenterY());
      if (collisionBoxCenter.distance(force.getLocation()) < ACCEPTABLE_DIST) {
        final double yDelta = this.getEntity().getHeight() - this.getEntity().getCollisionBox().getHeight() + this.getEntity().getCollisionBox().getHeight() / 2;
        final Point2D entityLocation = new Point2D.Double(force.getLocation().getX() - this.getEntity().getWidth() / 2, force.getLocation().getY() - yDelta);
        this.getEntity().setLocation(entityLocation);
      } else {
        final double angle = GeometricUtilities.calcRotationAngleInDegrees(collisionBoxCenter, force.getLocation());
        final boolean success = this.getPhysicsEngine().move(this.getEntity(), (float) angle, gameLoop.getDeltaTime() * 0.001f * force.getStrength());
        if (force.cancelOnCollision() && !success) {
          force.end();
        }
      }
    }
  }
}
