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
public class WASDEntityController extends ClientEntityMovementController implements IKeyObserver {
  private double velocityX, velocityY;

  private boolean movedX, movedY;
  private float dx;
  private float dy;

  public WASDEntityController(final IMovableEntity entity) {
    super(entity);

    Input.KEYBOARD.registerForKeyDownEvents(this);
  }

  @Override
  public void handlePressedKey(final KeyEvent keyCode) {

    switch (keyCode.getKeyCode()) {
    case KeyEvent.VK_W:
      this.dy--;
      this.movedY = true;
      break;
    case KeyEvent.VK_A:
      this.dx--;
      this.movedX = true;
      break;
    case KeyEvent.VK_S:
      this.movedY = true;
      this.dy++;
      break;
    case KeyEvent.VK_D:
      this.dx++;
      this.movedX = true;
      break;
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
