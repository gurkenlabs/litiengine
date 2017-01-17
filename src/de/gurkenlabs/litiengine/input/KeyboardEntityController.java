package de.gurkenlabs.litiengine.input;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.util.MathUtilities;

/**
 * TODO: Apply friction to terrain in order to slow down acceleration and speed
 * up deceleration.
 */
public class KeyboardEntityController extends ClientEntityMovementController implements IKeyObserver {
  private double velocityX, velocityY;
  private final int up, down, left, right;
  private boolean movedX, movedY;
  private float dx;
  private float dy;

  public KeyboardEntityController(final IMovableEntity entity) {
    this(entity, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D);
  }

  public KeyboardEntityController(final IMovableEntity entity, final int up, final int down, final int left, final int right) {
    super(entity);
    this.up = up;
    this.down = down;
    this.left = left;
    this.right = right;
    Input.KEYBOARD.registerForKeyDownEvents(this);
  }

  @Override
  public void handlePressedKey(final KeyEvent keyCode) {

    if (keyCode.getKeyCode() == this.up) {
      this.dy--;
      this.movedY = true;
    } else if (keyCode.getKeyCode() == this.down) {
      this.movedY = true;
      this.dy++;
    } else if (keyCode.getKeyCode() == this.left) {
      this.dx--;
      this.movedX = true;
    } else if (keyCode.getKeyCode() == this.right) {
      this.dx++;
      this.movedX = true;
    }
  }

  @Override
  public void handleReleasedKey(final KeyEvent keyCode) {

  }

  @Override
  public void handleTypedKey(final KeyEvent keyCode) {

  }

  @Override
  public void update(final IGameLoop loop) {
    super.update(loop);
    double maxPixelsPerTick = this.getControlledEntity().getVelocity() * 0.001 * Game.getConfiguration().CLIENT.getUpdaterate() * loop.getTimeScale();
    double inc = this.getControlledEntity().getAcceleration() == 0 ? maxPixelsPerTick : Game.getConfiguration().CLIENT.getUpdaterate() * 1.0 / this.getControlledEntity().getAcceleration() * maxPixelsPerTick;
    double dec = this.getControlledEntity().getDeceleration() == 0 ? maxPixelsPerTick : Game.getConfiguration().CLIENT.getUpdaterate() * 1.0 / this.getControlledEntity().getDeceleration() * maxPixelsPerTick;
    final double STOP_THRESHOLD = 0.1;

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

    final Point2D newLocation = new Point2D.Double(this.getControlledEntity().getLocation().getX() + this.velocityX, this.getControlledEntity().getLocation().getY() + this.velocityY);
    Game.getPhysicsEngine().move(this.getControlledEntity(), newLocation);
  }
}
