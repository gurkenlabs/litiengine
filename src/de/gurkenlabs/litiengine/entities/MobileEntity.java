package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.annotation.MovementInfo;
import de.gurkenlabs.util.geom.GeometricUtilities;

@MovementInfo
public class MobileEntity extends CollisionEntity implements IMobileEntity {
  private int acceleration;
  private int deceleration;
  private final List<Consumer<IMobileEntity>> entityMovedConsumer;
  private Point2D moveDestination;
  private boolean turnOnMove;

  private short velocity;

  public MobileEntity() {
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
  public Point2D getMoveDestination() {
    return this.moveDestination;
  }

  @Override
  public float getVelocity() {
    return this.velocity;
  }

  @Override
  public void onMoved(final Consumer<IMobileEntity> consumer) {
    if (this.entityMovedConsumer.contains(consumer)) {
      return;
    }

    this.entityMovedConsumer.add(consumer);
  }

  @Override
  public void setAcceleration(final int acceleration) {
    this.acceleration = acceleration;
  }

  @Override
  public void setDeceleration(final int deceleration) {
    this.deceleration = deceleration;
  }

  @Override
  public void setLocation(final Point2D position) {
    if (position == null || GeometricUtilities.equals(position, this.getLocation(), 0.001)) {
      return;
    }

    super.setLocation(position);

    for (final Consumer<IMobileEntity> consumer : this.entityMovedConsumer) {
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
  public void setVelocity(final short velocity) {
    this.velocity = velocity;
  }

  @Override
  public boolean turnOnMove() {
    return this.turnOnMove;
  }

}
