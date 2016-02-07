package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.annotation.MovementInfo;
import de.gurkenlabs.litiengine.input.IMovementController;
import de.gurkenlabs.util.geom.GeometricUtilities;

@MovementInfo
public abstract class MovableCombatEntity extends CombatEntity implements IMovableCombatEntity {

  private final List<Consumer<IMovableEntity>> entityMovedConsumer;
  private final short pixelsPerSecond;

  /** The direction. */
  private float facingAngle;

  /** The last moved. */
  private long lastMoved;

  private IMovementController movementController;

  public MovableCombatEntity() {
    this.entityMovedConsumer = new CopyOnWriteArrayList<>();
    final MovementInfo info = this.getClass().getAnnotation(MovementInfo.class);
    this.pixelsPerSecond = info.pixelsPerSecond();
  }

  /**
   * Gets the facing direction.
   *
   * @return the facing direction
   */
  @Override
  public float getFacingAngle() {
    return this.facingAngle;
  }

  @Override
  public Direction getFacingDirection() {
    if (this.getFacingAngle() >= 0 && this.getFacingAngle() < 45) {
      return Direction.DOWN;
    }
    if (this.getFacingAngle() >= 45 && this.getFacingAngle() < 135) {
      return Direction.RIGHT;
    }
    if (this.getFacingAngle() >= 135 && this.getFacingAngle() < 225) {
      return Direction.UP;
    }
    if (this.getFacingAngle() >= 225 && this.getFacingAngle() < 315) {
      return Direction.LEFT;
    }

    if (this.getFacingAngle() >= 315 && this.getFacingAngle() <= 360) {
      return Direction.DOWN;
    }

    System.out.println("unknown facing angle " + this.getFacingAngle());
    return Direction.UNDEFINED;
  }

  @Override
  public IMovementController getMovementController() {
    return this.movementController;
  }

  @Override
  public float getVelocityInPixelsPerSecond() {
    return this.pixelsPerSecond * this.getAttributes().getVelocity().getCurrentValue();
  }

  /**
   * Checks if is idle.
   *
   * @return true, if is idle
   */
  @Override
  public boolean isIdle() {
    final int IDLE_DELAY = 100;
    return System.currentTimeMillis() - this.lastMoved > IDLE_DELAY;
  }

  @Override
  public void onMoved(final Consumer<IMovableEntity> consumer) {
    if (this.entityMovedConsumer.contains(consumer)) {
      return;
    }

    this.entityMovedConsumer.add(consumer);
  }

  /**
   * Sets the facing direction.
   *
   * @param angle
   *          the new facing direction
   */
  @Override
  public void setFacingAngle(final float angle) {
    this.facingAngle = angle;
  }

  @Override
  public void setFacingDirection(final Direction facingDirection) {
    switch (facingDirection) {
    case DOWN:
      this.setFacingAngle(0);
      break;
    case RIGHT:
      this.setFacingAngle(90);
      break;
    case UP:
      this.setFacingAngle(180);
      break;
    case LEFT:
      this.setFacingAngle(270);
      break;

    default:
      return;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.entities.Entity#setMapLocation(java.awt.geom.Point2D)
   */
  @Override
  public void setLocation(final Point2D position) {
    if (position == null || GeometricUtilities.isEqual(position, this.getLocation(), 0.001)) {
      return;
    }

    super.setLocation(position);
    this.lastMoved = System.currentTimeMillis();

    for (final Consumer<IMovableEntity> consumer : this.entityMovedConsumer) {
      consumer.accept(this);
    }
  }

  @Override
  public void setMovementController(final IMovementController movementController) {
    this.movementController = movementController;
  }
}
