package de.gurkenlabs.litiengine.input;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.util.MathUtilities;
import de.gurkenlabs.util.geom.GeometricUtilities;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.java.games.input.ControllerEnvironment;

/**
 * TODO: Apply friction to terrain in order to slow down acceleration and speed
 * up deceleration.
 */
public class GamepadEntityController<T extends IMovableEntity> extends ClientEntityMovementController<T> {
  private double velocityX, velocityY;
  private boolean movedX, movedY;
  private float dx;
  private float dy;

  private Controller gamepad;

  public GamepadEntityController(final T entity) {
    super(entity);
    for (Controller controller : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
      Type type = controller.getType();

      if (type.equals(Type.GAMEPAD)) {
        this.gamepad = controller;
        break;
      }
    }

    if (gamepad == null) {
      throw new IllegalArgumentException("NO GAMEPAD FOUND!");
    }
  }

  @Override
  public void update(final IGameLoop loop) {
    this.retrieveGamepadValues();
    super.update(loop);
    final long deltaTime = loop.getDeltaTime();
    double maxPixelsPerTick = this.getEntity().getVelocity() * 0.001 * deltaTime;

    double inc = this.getEntity().getAcceleration() == 0 ? maxPixelsPerTick : deltaTime / (double) this.getEntity().getAcceleration() * maxPixelsPerTick;
    double dec = this.getEntity().getDeceleration() == 0 ? maxPixelsPerTick : deltaTime / (double) this.getEntity().getDeceleration() * maxPixelsPerTick;
    final double STOP_THRESHOLD = 0.1;

    if (this.movedX && this.movedY) {
      // we don't want the entity to move faster when moving diagonally
      // calculate a new x by dissolding the formula for diagonals of squares
      // sqrt(2 * x^2)
      inc /= Math.sqrt(2);
    }

    if (this.movedX) {
      this.velocityX += this.dx * inc;
      this.velocityX = MathUtilities.clamp(this.velocityX, -maxPixelsPerTick, maxPixelsPerTick);
      this.dx = 0;
      this.movedX = false;
    } else {
      if (this.velocityX > 0) {
        if (dec > this.velocityX) {
          this.velocityX = 0;
        } else {
          this.velocityX -= dec;
        }
      } else if (this.velocityX < 0) {
        if (dec < this.velocityX) {
          this.velocityX = 0;
        } else {
          this.velocityX += dec;
        }
      }

      if (Math.abs(this.velocityX) < STOP_THRESHOLD) {
        this.velocityX = 0;
      }
    }

    if (this.movedY) {
      this.velocityY += this.dy * inc;
      this.velocityY = MathUtilities.clamp(this.velocityY, -maxPixelsPerTick, maxPixelsPerTick);
      this.dy = 0;
      this.movedY = false;
    } else {
      if (this.velocityY > 0) {
        if (dec > this.velocityY) {
          this.velocityY = 0;
        } else {
          this.velocityY -= dec;
        }
      } else if (this.velocityY < 0) {
        if (dec < this.velocityY) {
          this.velocityY = 0;
        } else {
          this.velocityY += dec;
        }
      }

      if (Math.abs(this.velocityY) < STOP_THRESHOLD) {
        this.velocityY = 0;
      }
    }

    if (this.velocityX == 0 && this.velocityY == 0) {
      return;
    }

    final Point2D newLocation = new Point2D.Double(this.getEntity().getLocation().getX() + this.velocityX, this.getEntity().getLocation().getY() + this.velocityY);
    Game.getPhysicsEngine().move(this.getEntity(), newLocation);
  }

  private void retrieveGamepadValues() {
    this.gamepad.poll();
    
    float x = this.gamepad.getComponent(Identifier.Axis.X).getPollData();
    float y = this.gamepad.getComponent(Identifier.Axis.Y).getPollData();

    if (Math.abs(x) > 0.15) {
      this.dx = x;
      this.movedX = true;
    }
    
    if (Math.abs(y) > 0.15) {
      this.dy = y;
      this.movedY = true;
    }

    float rightX = this.gamepad.getComponent(Identifier.Axis.RX).getPollData();
    float rightY = this.gamepad.getComponent(Identifier.Axis.RY).getPollData();
    float targetX = 0, targetY = 0;
    if (Math.abs(rightX) > 0.05) {
      targetX = rightX;
    }
    if (Math.abs(rightY) > 0.05) {
      targetY = rightY;
    }
    
    if(targetX != 0 || targetY != 0){
      Point2D target = new Point2D.Double(this.getEntity().getDimensionCenter().getX() + targetX, this.getEntity().getDimensionCenter().getY() + targetY);
      double angle = GeometricUtilities.calcRotationAngleInDegrees(this.getEntity().getDimensionCenter(), target);
      this.getEntity().setAngle((float) angle);
    }
  }
}
