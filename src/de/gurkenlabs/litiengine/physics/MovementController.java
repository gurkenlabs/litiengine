package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

public class MovementController<T extends IMobileEntity> implements IMovementController {
  private static final double FORCE_APPLY_ACCEPTED_ERROR = 0.1;
  private final List<Force> activeForces;
  private final T mobileEntity;
  private final List<Predicate<IMobileEntity>> movementPredicates;
  private final List<Consumer<Point2D>> movedConsumer;

  public MovementController(final T mobileEntity) {
    this.activeForces = new CopyOnWriteArrayList<>();
    this.movementPredicates = new CopyOnWriteArrayList<>();
    this.movedConsumer = new CopyOnWriteArrayList<>();
    this.mobileEntity = mobileEntity;
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
    return this.mobileEntity;
  }

  @Override
  public void onMovementCheck(final Predicate<IMobileEntity> predicate) {
    if (!this.movementPredicates.contains(predicate)) {
      this.movementPredicates.add(predicate);
    }
  }

  @Override
  public void update() {
    this.handleForces();
  }

  @Override
  public void onMoved(Consumer<Point2D> cons) {
    this.movedConsumer.add(cons);
  }

  protected void moveEntity(double deltaX, double deltaY) {
    final Point2D newLocation = new Point2D.Double(this.getEntity().getLocation().getX() + deltaX, this.getEntity().getLocation().getY() + deltaY);
    final Point2D oldLocation = this.getEntity().getLocation();
    Game.getPhysicsEngine().move(this.getEntity(), newLocation);

    final Point2D delta = new Point2D.Double(this.getEntity().getX() - oldLocation.getX(), this.getEntity().getY() - oldLocation.getY());
    for (Consumer<Point2D> cons : this.movedConsumer) {
      cons.accept(delta);
    }
  }

  protected boolean isMovementAllowed() {
    for (final Predicate<IMobileEntity> predicate : this.movementPredicates) {
      if (!predicate.test(this.getEntity())) {
        return false;
      }
    }

    return true;
  }

  private void handleForces() {
    // clean up forces
    this.activeForces.forEach(x -> {
      if (x.hasEnded()) {
        this.activeForces.remove(x);
      }
    });

    for (final Force force : this.activeForces) {
      if (force.cancelOnReached() && force.hasReached(this.getEntity())) {
        force.end();
        continue;
      }

      final Point2D collisionBoxCenter = this.getEntity().getCollisionBoxCenter();
      if (collisionBoxCenter.distance(force.getLocation()) < FORCE_APPLY_ACCEPTED_ERROR) {
        final double yDelta = this.getEntity().getHeight() - this.getEntity().getCollisionBox().getHeight() + this.getEntity().getCollisionBox().getHeight() / 2;
        final Point2D entityLocation = new Point2D.Double(force.getLocation().getX() - this.getEntity().getWidth() / 2, force.getLocation().getY() - yDelta);
        this.getEntity().setLocation(entityLocation);
      } else {
        final double angle = GeometricUtilities.calcRotationAngleInDegrees(collisionBoxCenter, force.getLocation());
        final boolean success = Game.getPhysicsEngine().move(this.getEntity(), (float) angle, Game.getLoop().getDeltaTime() * 0.001f * force.getStrength() * Game.getLoop().getTimeScale());
        if (force.cancelOnCollision() && !success) {
          force.end();
        }
      }
    }
  }
}
