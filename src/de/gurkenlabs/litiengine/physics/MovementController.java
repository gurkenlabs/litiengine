package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

public class MovementController<T extends IMobileEntity> implements IMovementController {
  private final List<Force> activeForces;
  private final T mobileEntity;
  private final List<Predicate<IMobileEntity>> movementPredicates;
  private final List<Consumer<Point2D>> movedConsumer;

  private float dx;
  private float dy;
  private boolean movedX;
  private boolean movedY;
  private double velocityX;
  private double velocityY;

  public MovementController(final T mobileEntity) {
    this.activeForces = new CopyOnWriteArrayList<>();
    this.movementPredicates = new CopyOnWriteArrayList<>();
    this.movedConsumer = new CopyOnWriteArrayList<>();
    this.mobileEntity = mobileEntity;
  }

  @Override
  public void attach() {
    Game.loop().attach(this);
  }

  @Override
  public void detach() {
    Game.loop().detach(this);
  }

  @Override
  public void apply(final Force force) {
    if (!this.activeForces.contains(force)) {
      this.activeForces.add(force);
    }
  }

  @Override
  public List<Force> getActiveForces() {
    return this.activeForces;
  }

  @Override
  public T getEntity() {
    return this.mobileEntity;
  }

  @Override
  public float getDx() {
    return dx;
  }

  @Override
  public void setDx(float dx) {
    this.dx = dx;
    this.setMovedX(this.dx != 0);
  }

  @Override
  public float getDy() {
    return dy;
  }

  @Override
  public void setDy(float dy) {
    this.dy = dy;
    this.setMovedY(this.dy != 0);
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
    this.handleMovement();
  }

  @Override
  public void onMoved(Consumer<Point2D> cons) {
    this.movedConsumer.add(cons);
  }

  public void handleMovement() {
    if (!this.isMovementAllowed()) {
      this.velocityX = 0;
      this.velocityY = 0;
      return;
    }

    final double maxPixelsPerTick = this.getEntity().getTickVelocity();

    final double deltaTime = Game.loop().getDeltaTime() * Game.loop().getTimeScale();
    double accelerationRatio = deltaTime / (double) this.getEntity().getAcceleration();
    double decelerationRatio = deltaTime / (double) this.getEntity().getDeceleration();

    double inc = this.getEntity().getAcceleration() == 0 ? maxPixelsPerTick : accelerationRatio * maxPixelsPerTick;
    final double dec = this.getEntity().getDeceleration() == 0 ? maxPixelsPerTick : decelerationRatio * maxPixelsPerTick;

    if (this.isMovedX() && this.isMovedY()) {
      // we don't want the entity to move faster when moving diagonally
      // calculate a new x by dissolving the formula for diagonals of squares
      // sqrt(2 * x^2)
      inc /= (Math.sqrt(2) / Game.loop().getTimeScale());
    }

    // update velocity x
    if (this.isMovedX()) {
      double newVelocity = this.getVelocityX() + (this.getDx() > 0 ? inc : -inc);
      this.setVelocityX(MathUtilities.clamp(newVelocity, -maxPixelsPerTick, maxPixelsPerTick));
      this.setDx(0);
    } else {
      this.decelerateVelocityX(dec);
    }

    // update velocity y
    if (this.isMovedY()) {
      double newVelocity = this.getVelocityY() + (this.getDy() > 0 ? inc : -inc);
      newVelocity = MathUtilities.clamp(newVelocity, -maxPixelsPerTick, maxPixelsPerTick);
      this.setVelocityY(newVelocity);
      this.setDy(0);
    } else {
      this.decelerateVelocityY(dec);
    }

    if (this.getVelocityX() == 0 && this.getVelocityY() == 0) {
      return;
    }

    // actually move entity
    this.moveEntity(this.getVelocityX(), this.getVelocityY());
  }

  public boolean isMovedX() {
    return this.movedX;
  }

  public void setMovedX(boolean movedX) {
    this.movedX = movedX;
  }

  public boolean isMovedY() {
    return this.movedY;
  }

  public void setMovedY(boolean movedY) {
    this.movedY = movedY;
  }

  public double getVelocityX() {
    return velocityX;
  }

  protected void setVelocityX(double velocityX) {
    this.velocityX = velocityX;
  }

  protected void setVelocityY(double velocityY) {
    this.velocityY = velocityY;
  }

  public void decelerateVelocityX(double dec) {
    if (this.getVelocityX() > 0) {
      if (dec > this.getVelocityX()) {
        this.setVelocityX(0);
      } else {
        this.setVelocityX(this.getVelocityX() - dec);
      }
    } else if (this.getVelocityX() < 0) {
      if (dec < this.getVelocityX()) {
        this.setVelocityX(0);
      } else {
        this.setVelocityX(this.getVelocityX() + dec);
      }
    }

    if (Math.abs(this.getVelocityX()) < this.getStopThreshold()) {
      this.setVelocityX(0);
    }
  }

  public void decelerateVelocityY(double dec) {
    if (this.getVelocityY() > 0) {
      if (dec > this.getVelocityY()) {
        this.setVelocityY(0);
      } else {
        this.setVelocityY(this.getVelocityY() - dec);
      }
    } else if (this.getVelocityY() < 0) {
      if (dec < this.getVelocityY()) {
        this.setVelocityY(0);
      } else {
        this.setVelocityY(this.getVelocityY() + dec);
      }
    }

    if (Math.abs(this.getVelocityY()) < this.getStopThreshold()) {
      this.setVelocityY(0);
    }
  }

  public double getVelocityY() {
    return velocityY;
  }

  protected double getStopThreshold() {
    return 0.0025 * Game.loop().getDeltaTime();
  }

  protected void moveEntity(double deltaX, double deltaY) {
    final Point2D newLocation = new Point2D.Double(this.getEntity().getX() + deltaX, this.getEntity().getY() + deltaY);
    final Point2D oldLocation = this.getEntity().getLocation();
    Game.physics().move(this.getEntity(), newLocation);

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

    // disable turn-on-move for force handling
    boolean turn = this.getEntity().turnOnMove();
    this.getEntity().setTurnOnMove(false);
    try {
      double deltaX = 0;
      double deltaY = 0;
      for (final Force force : this.activeForces) {
        if (force.cancelOnReached() && force.hasReached(this.getEntity())) {
          force.end();
          continue;
        }

        final Point2D collisionBoxCenter = this.getEntity().getCollisionBoxCenter();
        final double angle = GeometricUtilities.calcRotationAngleInDegrees(collisionBoxCenter, force.getLocation());
        final double strength = Game.loop().getDeltaTime() * 0.001f * force.getStrength() * Game.loop().getTimeScale();
        deltaX += GeometricUtilities.getXDelta(angle, strength);
        deltaY += GeometricUtilities.getYDelta(angle, strength);
      }

      final Point2D target = new Point2D.Double(this.getEntity().getX() + deltaX, this.getEntity().getY() + deltaY);
      final boolean success = Game.physics().move(this.getEntity(), target);
      if (!success) {
        for (final Force force : this.activeForces) {
          if (force.cancelOnCollision()) {
            force.end();
          }
        }
      }
    } finally {
      this.getEntity().setTurnOnMove(turn);
    }
  }
}
