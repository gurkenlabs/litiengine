package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.MovementInfo;
import de.gurkenlabs.litiengine.graphics.animation.CreatureAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.EntityAnimationController;
import de.gurkenlabs.litiengine.physics.IMovementController;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

@MovementInfo
public class Creature extends CombatEntity implements IMobileEntity {
  private static final int IDLE_DELAY = 100;
  private int acceleration;
  private int deceleration;
  private long lastMoved;
  private Point2D moveDestination;
  private boolean turnOnMove;
  private short velocity;
  private String spritePrefix;

  public Creature() {
    this(null);
  }

  public Creature(String spritePrefix) {
    super();
    final MovementInfo movementInfo = this.getClass().getAnnotation(MovementInfo.class);
    if (movementInfo != null) {
      this.velocity = movementInfo.velocity();
      this.acceleration = movementInfo.acceleration();
      this.deceleration = movementInfo.deceleration();
      this.setTurnOnMove(movementInfo.turnOnMove());
    }

    if (spritePrefix != null) {
      this.setSpritePrefix(spritePrefix);
    } else {
      this.setSpritePrefix(ArrayUtilities.getRandom(EntityAnimationController.getDefaultSpritePrefixes(this.getClass())));
    }

    this.addController(new CreatureAnimationController<Creature>(this, true));
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
  public Point2D getMoveDestination() {
    return this.moveDestination;
  }

  @Override
  public IMovementController getMovementController() {
    return this.getController(IMovementController.class);
  }

  /**
   * Gets the current sprite prefix of this instance. Overwriting this allows
   * for a more sophisticated logic that determines the sprite to be used; e.g.
   * This method could append certain properties of the creature (state, weapon,
   * ...) to the default string. <br>
   * <br>
   * The value of this method will be used e.g. by the
   * {@link CreatureAnimationController} to determine the animation that it
   * should play.
   * 
   * @return The current sprite prefix of this instance.
   */
  public String getSpritePrefix() {
    return this.spritePrefix;
  }

  @Override
  public float getTickVelocity() {
    return MobileEntity.getTickVelocity(this);
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
  public boolean isIdle() {
    return Game.getLoop().getDeltaTime(this.lastMoved) > IDLE_DELAY;
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
    this.setAngle(Direction.toAngle(facingDirection));
  }

  @Override
  public void setLocation(final Point2D position) {
    if (this.isDead() || position == null || GeometricUtilities.equals(position, this.getLocation(), 0.001)) {
      return;
    }

    super.setLocation(position);

    if (Game.hasStarted()) {
      this.lastMoved = Game.getLoop().getTicks();
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

  public void setSpritePrefix(String spritePrefix) {
    this.spritePrefix = spritePrefix;
  }

  @Override
  public boolean turnOnMove() {
    return this.turnOnMove;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (this.getName() != null && !this.getName().isEmpty()) {
      sb.append(this.getName());
    } else {
      sb.append(Creature.class.getSimpleName());
    }
    sb.append(" (");
    sb.append(this.getSpritePrefix());

    sb.append(") #");
    sb.append(this.getMapId());
    return sb.toString();
  }
}