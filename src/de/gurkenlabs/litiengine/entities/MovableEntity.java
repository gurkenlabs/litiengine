package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.annotation.MovementInfo;
import de.gurkenlabs.litiengine.physics.IEntityMovementController;
import de.gurkenlabs.util.geom.GeometricUtilities;

@MovementInfo
public class MovableEntity extends CollisionEntity implements IMovableEntity {
  private final List<Consumer<IMovableEntity>> entityMovedConsumer;
  private final short velocity;

  private IEntityMovementController movementController;

  public MovableEntity() {
    this.entityMovedConsumer = new CopyOnWriteArrayList<>();
    final MovementInfo info = this.getClass().getAnnotation(MovementInfo.class);
    this.velocity = info.velocity();
  }

  @Override
  public float getVelocity() {
    return this.velocity;
  }

  @Override
  public IEntityMovementController getMovementController() {
    return this.movementController;
  }

  @Override
  public void onMoved(final Consumer<IMovableEntity> consumer) {
    if (this.entityMovedConsumer.contains(consumer)) {
      return;
    }

    this.entityMovedConsumer.add(consumer);
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
  public void setMovementController(final IEntityMovementController movementController) {
    this.movementController = movementController;
  }

  @Override
  public void setAngle(final float angle) {
    super.setAngle(angle);
  }
}
