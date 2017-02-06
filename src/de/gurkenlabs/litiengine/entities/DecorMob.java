package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.Random;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.annotation.CombatAttributesInfo;
import de.gurkenlabs.litiengine.graphics.animation.DecorMobAnimationController;
import de.gurkenlabs.litiengine.input.ClientEntityMovementController;

@CombatAttributesInfo(health = 1)
public class DecorMob extends MovableCombatEntity {
  private final String mobType;
  private final MovementBehaviour behaviour;

  public DecorMob(final Point2D location, final String mobType, final MovementBehaviour behaviour, final short velocity) {
    super();
    this.mobType = mobType;
    this.setLocation(location);
    
    Game.getEntityControllerManager().addController(this, new DecorMobAnimationController(this));
    this.behaviour = behaviour;
    switch (this.behaviour) {
    case SHY:
      Game.getEntityControllerManager().addController(this, new ShyDecorMobMovementController(this));
      break;
    case RANDOM:
      break;
    default:
      break;
    }

    this.setVelocity(velocity);
  }

  public String getMobType() {
    return this.mobType;
  }

  public enum MovementBehaviour {
    IDLE,

    // butterfly
    SHY,

    // rabbit
    RANDOM;

    public static MovementBehaviour get(String behaviour) {
      if (behaviour == null || behaviour.isEmpty()) {
        return MovementBehaviour.IDLE;
      }

      try {
        return MovementBehaviour.valueOf(behaviour);
      } catch (IllegalArgumentException iae) {
        return MovementBehaviour.IDLE;
      }
    }
  }

  private class ShyDecorMobMovementController extends ClientEntityMovementController<DecorMob> {
    private long lastAngleChange;
    private long nextAngleChange;
    private int angle;

    public ShyDecorMobMovementController(final DecorMob movableEntity) {
      super(movableEntity);
      this.calculateNextAngleChange();
    }

    @Override
    public void update(final IGameLoop gameLoop) {
      super.update(gameLoop);
      if (Game.getEnvironment() == null || isDead()) {
        return;
      }
      final long currentTick = gameLoop.getTicks();
      final long timeSinceLastAngleChange = gameLoop.getDeltaTime(this.lastAngleChange);
      if (this.angle == 0 || timeSinceLastAngleChange > this.nextAngleChange) {
        final Random rand = new Random();
        this.angle = rand.nextInt(360);
        this.lastAngleChange = currentTick;
        this.calculateNextAngleChange();
      }

      final float pixelsPerTick = gameLoop.getDeltaTime() * 0.001F * this.getEntity().getVelocity();
      this.getPhysicsEngine().move(this.getEntity(), this.angle, pixelsPerTick);
      /*
       * for (final IMovableEntity mob :
       * Game.getEnvironment().getMovableEntities()) { if (!mob.equals(this) &&
       * mob.getLocation().distance(this.getControlledEntity().getLocation()) <
       * DETECTION_RADIUS) { final float angle = (float)
       * (GeometricUtilities.calcRotationAngleInDegrees(this.getControlledEntity
       * ().getDimensionCenter(), mob.getDimensionCenter()) - 180);
       * this.getPhysicsEngine().move(this.getControlledEntity(), angle,
       * pixelsPerTick * 5); } }
       */
    }

    public void calculateNextAngleChange() {
      this.nextAngleChange = new Random().nextInt(3000) + 2000;
    }

  }
}
