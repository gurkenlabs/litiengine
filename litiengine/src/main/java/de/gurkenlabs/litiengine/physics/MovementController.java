package de.gurkenlabs.litiengine.physics;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public class MovementController<T extends IMobileEntity> implements IMovementController {

  private final List<Force> activeForces;
  private final T mobileEntity;
  private final List<Predicate<IMobileEntity>> movementPredicates;

  private float dx;
  private float dy;
  private double velocity;
  private double moveAngle;

  public MovementController(final T mobileEntity) {
    this.activeForces = new CopyOnWriteArrayList<>();
    this.movementPredicates = new CopyOnWriteArrayList<>();
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
  }

  @Override
  public float getDy() {
    return dy;
  }

  @Override
  public void setDy(float dy) {
    this.dy = dy;
  }

  @Override
  public void onMovementCheck(final Predicate<IMobileEntity> predicate) {
    if (!this.movementPredicates.contains(predicate)) {
      this.movementPredicates.add(predicate);
    }
  }

  @Override
  public void update() {
    handleForces();
    handleMovement();
  }

  public void handleMovement() {
    if (!this.isMovementAllowed()) {
      this.velocity = 0;
      return;
    }

    final double deltaTime = Game.loop().getDeltaTime() * Game.loop().getTimeScale();

    final double acceleration = getEntity().getAcceleration(deltaTime);
    final double deceleration = getEntity().getDeceleration(deltaTime, getVelocity());

    double dxTemp = getDx();
    double dyTemp = getDy();
    this.setDx(0);
    this.setDy(0);

    final double deltaVelocity =
      Math.min(Math.sqrt(Math.pow(dxTemp, 2) + Math.pow(dyTemp, 2)), acceleration);
    if (deltaVelocity != 0) {
      double newVelocity = this.getVelocity() + deltaVelocity;
      this.setVelocity(newVelocity);
    } else {
      final double newVelocity = Math.max(0, this.getVelocity() - deceleration);
      this.setVelocity(newVelocity);
      dxTemp = GeometricUtilities.getDeltaX(this.moveAngle);
      dyTemp = GeometricUtilities.getDeltaY(this.moveAngle);
    }

    if (this.getVelocity() == 0) {
      this.moveAngle = 0;
      return;
    }

    // actually move entity
    this.moveEntity(dxTemp, dyTemp);
  }

  @Override
  public Force getForce(String identifier) {
    if (identifier == null || identifier.isEmpty()) {
      return null;
    }

    return this.getActiveForces().stream()
      .filter(x -> x.getIdentifier() != null && x.getIdentifier().equals(identifier))
      .findFirst()
      .orElse(null);
  }

  @Override
  public double getMoveAngle() {
    return this.moveAngle;
  }

  @Override
  public void setVelocity(double velocity) {
    final double maxVelocity = getEntity().getTickVelocity();
    this.velocity = MathUtilities.clamp(velocity, -maxVelocity, maxVelocity);
  }

  @Override
  public double getVelocity() {
    return this.velocity;
  }

  protected void moveEntity(double deltaX, double deltaY) {
    this.moveAngle = Math.toDegrees(Math.atan2(deltaX, deltaY));
    Game.physics().move(getEntity(), this.moveAngle, this.getVelocity());
  }

  protected boolean isMovementAllowed() {
    return movementPredicates.stream().allMatch(p -> p.test(getEntity()));
  }

  private void handleForces() {
    // clean up forces
    if (this.activeForces.isEmpty()) {
      return;
    }
    this.activeForces.removeIf(Force::hasEnded);
    moveEntityByActiveForces();
  }

  private void moveEntityByActiveForces() {
    final Point2D combinedForcesVector = combineActiveForces();
    final Point2D target =
      new Point2D.Double(
        getEntity().getX() + combinedForcesVector.getX(),
        getEntity().getY() + combinedForcesVector.getY());

    final boolean success = Game.physics().move(getEntity(), target, false);
    if (!success) {
      activeForces.stream().filter(Force::cancelOnCollision).forEach(Force::end);
    }
  }

  private Point2D combineActiveForces() {
    double deltaX = 0;
    double deltaY = 0;
    for (final Force force : this.activeForces) {
      if (force.cancelOnReached() && force.hasReached(getEntity())) {
        force.end();
        continue;
      }

      final double angle =
        GeometricUtilities.calcRotationAngleInDegrees(getEntity().getCollisionBoxCenter(),
          force.getLocation());
      final double strength =
        Game.loop().getDeltaTime() * 0.001f * force.getStrength() * Game.loop().getTimeScale();
      deltaX += GeometricUtilities.getDeltaX(angle, strength);
      deltaY += GeometricUtilities.getDeltaY(angle, strength);
    }

    return new Point2D.Double(deltaX, deltaY);
  }
}
