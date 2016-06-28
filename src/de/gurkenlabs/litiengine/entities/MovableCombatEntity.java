package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.annotation.MovementInfo;
import de.gurkenlabs.litiengine.physics.IEntityMovementController;
import de.gurkenlabs.util.geom.GeometricUtilities;

@MovementInfo
public abstract class MovableCombatEntity extends CombatEntity implements IMovableCombatEntity {

  private final List<Consumer<IMovableEntity>> entityMovedConsumer;
  private final short velocity;
  private boolean turnOnMove;
  
  /** The last moved. */
  private long lastMoved;

  private IEntityMovementController movementController;

  public MovableCombatEntity() {
    super();
    this.entityMovedConsumer = new CopyOnWriteArrayList<>();
    final MovementInfo info = this.getClass().getAnnotation(MovementInfo.class);
    this.velocity = info.velocity();
  }

  @Override
  public Direction getFacingDirection() {
    return Direction.fromAngle(this.getAngle());
  }

  @Override
  public IEntityMovementController getMovementController() {
    return this.movementController;
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
  public void setMovementController(final IEntityMovementController movementController) {
    this.movementController = movementController;
  }
  
  @Override
  public boolean turnOnMove() {
    return this.turnOnMove;
  }

  @Override
  public void setTurnOnMove(boolean turn) {
    this.turnOnMove = turn;
  }
}
