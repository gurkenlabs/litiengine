package de.gurkenlabs.litiengine.input;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.physics.MovementController;
import de.gurkenlabs.util.MathUtilities;
import de.gurkenlabs.util.geom.GeometricUtilities;
import net.java.games.input.Component.Identifier;

public class GamepadEntityController<T extends IMovableEntity> extends MovementController<T> {
  private static final double STOP_THRESHOLD = 0.1;
  private float dx;
  private float dy;
  private int gamePadIndex = -1;
  private boolean movedX;
  private boolean movedY;

  private double velocityX;
  private double velocityY;

  public GamepadEntityController(final T entity) {
    super(entity);

    Input.gamepadManager().onGamepadAdded(pad -> {
      if (this.gamePadIndex == -1) {
        this.gamePadIndex = pad.getIndex();
      }
    });

    Input.gamepadManager().onGamepadRemoved(pad -> {
      if (this.gamePadIndex == pad.getIndex()) {
        this.gamePadIndex = -1;
        final IGamepad newGamePad = Input.getGamepad();
        if (newGamePad != null) {
          this.gamePadIndex = newGamePad.getIndex();
        }
      }
    });
  }

  @Override
  public void update(final IGameLoop loop) {
    if (!this.isMovementAllowed()) {
      return;
    }

    this.retrieveGamepadValues();
    super.update(loop);
    final long deltaTime = loop.getDeltaTime();
    final double maxPixelsPerTick = this.getEntity().getVelocity() * 0.001 * deltaTime;

    double inc = this.getEntity().getAcceleration() == 0 ? maxPixelsPerTick : deltaTime / (double) this.getEntity().getAcceleration() * maxPixelsPerTick;
    final double dec = this.getEntity().getDeceleration() == 0 ? maxPixelsPerTick : deltaTime / (double) this.getEntity().getDeceleration() * maxPixelsPerTick;

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
    if (this.gamePadIndex == -1 || this.gamePadIndex != -1 && Input.getGamepad(this.gamePadIndex) == null) {
      return;
    }

    final float x = Input.getGamepad(this.gamePadIndex).getPollData(Identifier.Axis.X);
    final float y = Input.getGamepad(this.gamePadIndex).getPollData(Identifier.Axis.Y);

    if (Math.abs(x) > 0.15) {
      this.dx = x;
      this.movedX = true;
    }

    if (Math.abs(y) > 0.15) {
      this.dy = y;
      this.movedY = true;
    }

    final float rightX = Input.getGamepad(this.gamePadIndex).getPollData(Identifier.Axis.RX);
    final float rightY = Input.getGamepad(this.gamePadIndex).getPollData(Identifier.Axis.RY);
    float targetX = 0;
    float targetY = 0;
    if (Math.abs(rightX) > 0.08) {
      targetX = rightX;
    }
    if (Math.abs(rightY) > 0.08) {
      targetY = rightY;
    }

    if (targetX != 0 || targetY != 0) {
      final Point2D target = new Point2D.Double(this.getEntity().getDimensionCenter().getX() + targetX, this.getEntity().getDimensionCenter().getY() + targetY);
      final double angle = GeometricUtilities.calcRotationAngleInDegrees(this.getEntity().getDimensionCenter(), target);
      this.getEntity().setAngle((float) angle);
    }
  }
}
