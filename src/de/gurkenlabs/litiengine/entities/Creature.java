package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.attributes.Attribute;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import de.gurkenlabs.litiengine.graphics.animation.CreatureAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.EntityAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;
import de.gurkenlabs.litiengine.physics.IMovementController;
import de.gurkenlabs.litiengine.physics.MovementController;

/**
 * TODO: Add idle event
 */
@MovementInfo
@TmxType(MapObjectType.CREATURE)
public class Creature extends CombatEntity implements IMobileEntity {
  private static final int IDLE_DELAY = 100;
  private final Collection<EntityMovedListener> movedListeners = ConcurrentHashMap.newKeySet();

  @TmxProperty(name = MapObjectProperty.MOVEMENT_ACCELERATION)
  private int acceleration;

  @TmxProperty(name = MapObjectProperty.MOVEMENT_DECELERATION)
  private int deceleration;

  @TmxProperty(name = MapObjectProperty.MOVEMENT_TURNONMOVE)
  private boolean turnOnMove;

  @TmxProperty(name = MapObjectProperty.MOVEMENT_VELOCITY)
  private Attribute<Float> velocity;

  @TmxProperty(name = MapObjectProperty.SPRITESHEETNAME)
  private String spritesheetName;

  @TmxProperty(name = MapObjectProperty.SCALE_SPRITE)
  private boolean scaling;

  private long lastMoved;

  public Creature() {
    this(null);
  }

  /**
   * Instantiates a new <code>Creature</code> entity.
   *
   * @param spritesheetName
   *          The spritesheet name that identifies the sprites bound to this instance.
   * 
   * @see CreatureAnimationController#getSpriteName(Creature, de.gurkenlabs.litiengine.graphics.CreatureAnimationState)
   */
  public Creature(String spritesheetName) {
    super();
    final MovementInfo movementInfo = this.getClass().getAnnotation(MovementInfo.class);
    if (movementInfo != null) {
      this.velocity = new Attribute<>(movementInfo.velocity());
      this.acceleration = movementInfo.acceleration();
      this.deceleration = movementInfo.deceleration();
      this.setTurnOnMove(movementInfo.turnOnMove());
      this.addController(this.createMovementController());
    }

    if (spritesheetName != null) {
      this.setSpritesheetName(spritesheetName);
    } else {
      this.setSpritesheetName(Game.random().choose(EntityAnimationController.getDefaultSpritePrefixes(this.getClass())));
    }
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

  public Direction getFacingDirection() {
    return Direction.fromAngle(this.getAngle());
  }

  @Override
  public IMovementController getMovementController() {
    return this.getController(IMovementController.class);
  }

  /**
   * Gets the current spritesheet name of this instance. Overwriting this allows
   * for a more sophisticated logic that determines the sprite to be used; e.g.
   * This method could append certain properties of the creature (state, weapon,
   * ...) to the default string. <br>
   * <br>
   * The value of this method will be used e.g. by the
   * {@link CreatureAnimationController} to determine the animation that it
   * should play.
   * 
   * @return The current spritesheet name of this instance.
   */
  public String getSpritesheetName() {
    return this.spritesheetName;
  }

  @Override
  public float getTickVelocity() {
    return MobileEntity.getTickVelocity(this);
  }

  @Override
  public Attribute<Float> getVelocity() {
    return this.velocity;
  }

  public boolean isScaling() {
    return this.scaling;
  }

  /**
   * Checks if is idle.
   *
   * @return true, if is idle
   */
  public boolean isIdle() {
    return Game.time().since(this.lastMoved) > IDLE_DELAY;
  }

  @Override
  public void setAcceleration(final int acceleration) {
    this.acceleration = acceleration;
  }

  @Override
  public void setDeceleration(final int deceleration) {
    this.deceleration = deceleration;
  }

  public void setFacingDirection(final Direction facingDirection) {
    this.setAngle(facingDirection.toAngle());
  }

  @Override
  public void setLocation(final Point2D position) {
    if (this.isDead() || position == null) {
      return;
    }

    super.setLocation(position);

    if (Game.hasStarted()) {
      this.lastMoved = Game.time().now();
    }
  }

  @Override
  public void setTurnOnMove(final boolean turn) {
    this.turnOnMove = turn;
  }

  public void setSpritesheetName(String spritesheetName) {
    if (this.spritesheetName != null && this.spritesheetName.equals(spritesheetName)) {
      return;
    }

    this.spritesheetName = spritesheetName;
    this.updateAnimationController();
  }

  public void setScaling(boolean scaling) {
    this.scaling = scaling;
  }

  @Override
  public void setVelocity(float velocity) {
    this.getVelocity().setBaseValue(velocity);
  }

  @Override
  public boolean turnOnMove() {
    return this.turnOnMove;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("#" + this.getMapId() + ": ");
    if (this.getName() != null && !this.getName().isEmpty()) {
      sb.append(this.getName());
    } else {
      sb.append(Creature.class.getSimpleName());
    }
    sb.append(" (" + this.getSpritesheetName() + ")");

    return sb.toString();
  }

  protected void updateAnimationController() {
    IEntityAnimationController<?> controller = this.createAnimationController();
    this.getControllers().addController(controller);
    if (Game.world().environment() != null && Game.world().environment().isLoaded()) {
      Game.loop().attach(controller);
    }
  }

  protected IEntityAnimationController<?> createAnimationController() {
    return new CreatureAnimationController<>(this, true);
  }

  protected IMovementController createMovementController() {
    return new MovementController<>(this);
  }
}