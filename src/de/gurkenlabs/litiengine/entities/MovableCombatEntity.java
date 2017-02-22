package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.annotation.MovementInfo;
import de.gurkenlabs.util.geom.GeometricUtilities;

@MovementInfo
public class MovableCombatEntity extends CombatEntity implements IMovableCombatEntity {

  private int acceleration;
  private int deceleration;
  private final List<Consumer<IMovableEntity>> entityMovedConsumer;
  /** The last moved. */
  private long lastMoved;

  private Point2D moveDestination;
  private boolean turnOnMove;

  private short velocity;

  public MovableCombatEntity() {
    super();
    this.entityMovedConsumer = new CopyOnWriteArrayList<>();
    final MovementInfo info = this.getClass().getAnnotation(MovementInfo.class);
    this.velocity = info.velocity();
    this.acceleration = info.acceleration();
    this.deceleration = info.deceleration();
    this.setTurnOnMove(info.turnOnMove());
  }

  @Override
  public int getAcceleration() {
    return this.acceleration;
  }

  @Override
  public int getDeceleration() {
    return this.deceleration;
  }

  @Override
  public Direction getFacingDirection() {
    return Direction.fromAngle(this.getAngle());
  }

  @Override
  public Point2D getMoveDestination() {
    return this.moveDestination;
  }

  @Override
  public float getVelocity() {
    return this.velocity * this.getAttributes().getVelocity().getCurrentValue();
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

  @Override
  public void setAcceleration(final int acceleration) {
    this.acceleration = acceleration;
  }

  /**
   * Sets the facing direction.
   *
   * @param angle
   *          the new facing direction
   */
  @Override
  public void setAngle(final float angle) {
    super.setAngle(angle);
  }

  @Override
  public void setDeceleration(final int deceleration) {
    this.deceleration = deceleration;
  }

  @Override
  public void setFacingDirection(final Direction facingDirection) {
    this.setAngle(Direction.toAngle(facingDirection));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.entities.Entity#setMapLocation(java.awt.geom.Point2D)
   */
  @Override
  public void setLocation(final Point2D position) {
    if (this.isDead() || position == null || GeometricUtilities.equals(position, this.getLocation(), 0.001)) {
      return;
    }

    super.setLocation(position);
    this.lastMoved = System.currentTimeMillis();

    for (final Consumer<IMovableEntity> consumer : this.entityMovedConsumer) {
      consumer.accept(this);
    }
  }

  @Override
  public void setMoveDestination(final Point2D dest) {
    this.moveDestination = dest;
    this.setAngle((float) GeometricUtilities.calcRotationAngleInDegrees(this.getLocation(), this.getMoveDestination()));
  }

  @Override
  public void setTurnOnMove(final boolean turn) {
    this.turnOnMove = turn;
  }

  @Override
  public void setVelocity(final short velocity) {
    this.velocity = velocity;
  }

  @Override
  public boolean turnOnMove() {
    return this.turnOnMove;
  }
}