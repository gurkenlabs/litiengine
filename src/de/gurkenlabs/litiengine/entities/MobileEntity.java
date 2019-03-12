package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameLoop;
import de.gurkenlabs.litiengine.annotation.MovementInfo;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.physics.IMovementController;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

@MovementInfo
public class MobileEntity extends CollisionEntity implements IMobileEntity {
  @TmxProperty(name = MapObjectProperty.MOVEMENT_ACCELERATION)
  private int acceleration;

  @TmxProperty(name = MapObjectProperty.MOVEMENT_DECELERATION)
  private int deceleration;

  @TmxProperty(name = MapObjectProperty.MOVEMENT_TURNONMOVE)
  private boolean turnOnMove;

  @TmxProperty(name = MapObjectProperty.MOVEMENT_VELOCITY)
  private Attribute<Float> velocity;

  private Point2D moveDestination;

  public MobileEntity() {
    final MovementInfo info = this.getClass().getAnnotation(MovementInfo.class);
    this.velocity = new Attribute<>(info.velocity());
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
  public float getTickVelocity() {
    return getTickVelocity(this);
  }

  @Override
  public Attribute<Float> getVelocity() {
    return this.velocity;
  }

  @Override
  public IMovementController getMovementController() {
    return this.getController(IMovementController.class);
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
  public void setVelocity(float velocity) {
    this.getVelocity().setBaseValue(velocity);
  }

  @Override
  public boolean turnOnMove() {
    return this.turnOnMove;
  }

  protected static float getTickVelocity(IMobileEntity entity) {
    // pixels per ms multiplied by the passed ms
    // ensure that entities don't travel too far in case of lag
    return Math.min(Game.loop().getDeltaTime(), GameLoop.TICK_DELTATIME_LAG) * 0.001F * entity.getVelocity().getCurrentValue() * Game.loop().getTimeScale();
  }
}
