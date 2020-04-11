package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameLoop;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.physics.IMovementController;
import de.gurkenlabs.litiengine.physics.MovementController;

@MovementInfo
public class MobileEntity extends CollisionEntity implements IMobileEntity {
  private final Collection<EntityMovedListener> movedListeners = ConcurrentHashMap.newKeySet();

  @TmxProperty(name = MapObjectProperty.MOVEMENT_ACCELERATION)
  private int acceleration;

  @TmxProperty(name = MapObjectProperty.MOVEMENT_DECELERATION)
  private int deceleration;

  @TmxProperty(name = MapObjectProperty.MOVEMENT_TURNONMOVE)
  private boolean turnOnMove;

  @TmxProperty(name = MapObjectProperty.MOVEMENT_VELOCITY)
  private Attribute<Float> velocity;

  /**
   * Instantiates a new <code>MobileEntity</code>.
   */
  public MobileEntity() {
    final MovementInfo info = this.getClass().getAnnotation(MovementInfo.class);
    this.velocity = new Attribute<>(info.velocity());
    this.acceleration = info.acceleration();
    this.deceleration = info.deceleration();
    this.setTurnOnMove(info.turnOnMove());
    this.addController(new MovementController<>(this));
  }

  @Override
  public void onMoved(EntityMovedListener listener) {
    this.movedListeners.add(listener);
  }

  @Override
  public void removeMovedListener(EntityMovedListener listener) {
    this.movedListeners.remove(listener);
  }

  @Override
  public void fireMovedEvent(EntityMovedEvent event) {
    for (EntityMovedListener listener : this.movedListeners) {
      listener.moved(event);
    }
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
  public float getTickVelocity() {
    return getTickVelocity(this);
  }

  @Override
  public Attribute<Float> getVelocity() {
    return this.velocity;
  }

  @Override
  public IMovementController movement() {
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
    if (position == null) {
      return;
    }

    super.setLocation(position);
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
    return Math.min(Game.loop().getDeltaTime(), GameLoop.TICK_DELTATIME_LAG) * 0.001F * entity.getVelocity().get() * Game.loop().getTimeScale();
  }
}
