package de.gurkenlabs.litiengine.physics;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.util.MathUtilities;

public abstract class AccelerationMovementController<T extends IMobileEntity> extends MovementController<T> {
  private float dx;
  private float dy;
  private boolean movedX;
  private boolean movedY;
  private double velocityX;
  private double velocityY;

  public AccelerationMovementController(T mobileEntity) {
    super(mobileEntity);
  }

  @Override
  public void update() {
    super.update();
    if (!this.isMovementAllowed()) {
      this.velocityX = 0;
      this.velocityY = 0;
      return;
    }

    final double maxPixelsPerTick = this.getEntity().getTickVelocity();

    final long deltaTime = Game.loop().getDeltaTime();
    double accelerationRatio = (double) deltaTime / (double) this.getEntity().getAcceleration();
    double decelerationRatio = (double) deltaTime / (double) this.getEntity().getDeceleration();

    double inc = this.getEntity().getAcceleration() == 0 ? maxPixelsPerTick : accelerationRatio * maxPixelsPerTick;
    final double dec = this.getEntity().getDeceleration() == 0 ? maxPixelsPerTick : decelerationRatio * maxPixelsPerTick;

    if (this.isMovedX() && this.isMovedY()) {
      // we don't want the entity to move faster when moving diagonally
      // calculate a new x by dissolving the formula for diagonals of squares
      // sqrt(2 * x^2)
      inc /= Math.sqrt(2);
    }

    // update velocity x
    if (this.isMovedX()) {
      double newVelocity = this.getVelocityX() + (this.getDx() > 0 ? inc : -inc);
      this.setVelocityX(MathUtilities.clamp(newVelocity, -maxPixelsPerTick, maxPixelsPerTick));
      this.setDx(0);
    } else {
      this.decellerateVelocityX(dec);
    }

    // update velocity y
    if (this.isMovedY()) {
      double newVelocity = this.getVelocityY() + (this.getDy() > 0 ? inc : -inc);
      this.setVelocityY(MathUtilities.clamp(newVelocity, -maxPixelsPerTick, maxPixelsPerTick));
      this.setDy(0);
    } else {
      this.decellerateVelocityY(dec);
    }

    if (this.getVelocityX() == 0 && this.getVelocityY() == 0) {
      return;
    }

    // actually move entity
    this.moveEntity(this.getVelocityX(), this.getVelocityY());
  }

  public float getDx() {
    return dx;
  }

  public void setDx(float dx) {
    this.dx = dx;
    this.setMovedX(this.dx != 0);
  }

  public float getDy() {
    return dy;
  }

  public void setDy(float dy) {
    this.dy = dy;
    this.setMovedY(this.dy != 0);
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

  public void setVelocityX(double velocityX) {
    this.velocityX = velocityX;
  }

  public void decellerateVelocityX(double dec) {
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

  public void decellerateVelocityY(double dec) {
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

  public void setVelocityY(double velocityY) {
    this.velocityY = velocityY;
  }

  protected double getStopThreshold() {
    return 0.0025 * Game.loop().getDeltaTime();
  }
}
