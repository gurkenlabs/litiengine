package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.annotation.MovementInfo;
import de.gurkenlabs.litiengine.physics.IMovementController;
import de.gurkenlabs.util.geom.GeometricUtilities;

@MovementInfo
public class MovableEntity extends CollisionEntity implements IMovableEntity {
  private final List<Consumer<IMovableEntity>> entityMovedConsumer;
  private short velocity;
  private int acceleration;
  private int deceleration;
  private Point2D moveDestination;

  private boolean turnOnMove;


  public MovableEntity() {
    this.entityMovedConsumer = new CopyOnWriteArrayList<>();
    final MovementInfo info = this.getClass().getAnnotation(MovementInfo.class);
    this.velocity = info.velocity();
    this.acceleration = info.acceleration();
    this.deceleration = info.deceleration();
    this.setTurnOnMove(info.turnOnMove());
  }

  @Override
  public Point2D getMoveDestination() {
    return this.moveDestination;
  }


  @Override
  public float getVelocity() {
    return this.velocity;
  }

  public void setVelocity(short velocity) {
    this.velocity = velocity;
  }

  public void setAcceleration(int acceleration) {
    this.acceleration = acceleration;
  }

  @Override
  public void onMoved(final Consumer<IMovableEntity> consumer) {
    if (this.entityMovedConsumer.contains(consumer)) {
      return;
    }

    this.entityMovedConsumer.add(consumer);
  }

  @Override
  public void setAngle(final float angle) {
    super.setAngle(angle);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.entities.Entity#setMapLocation(java.awt.geom.Point2D)
   */
  @Override
  public void setLocation(final Point2D position) {
    if (position == null || GeometricUtilities.equals(position, this.getLocation(), 0.001)) {
      return;
    }

    super.setLocation(position);

    for (final Consumer<IMovableEntity> consumer : this.entityMovedConsumer) {
      consumer.accept(this);
    }
  }

  @Override
  public void setMoveDestination(final Point2D dest) {
    this.moveDestination = dest;
  }

  @Override
  public void setTurnOnMove(final boolean turn) {
    this.turnOnMove = turn;
  }

  @Override
  public boolean turnOnMove() {
    return this.turnOnMove;
  }

  @Override
  public int getAcceleration() {
    return this.acceleration;
  }

  public int getDeceleration() {
    return this.deceleration;
  }

  public void setDeceleration(int deceleration) {
    this.deceleration = deceleration;
  }
  
  
}
