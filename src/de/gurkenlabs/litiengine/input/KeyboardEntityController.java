package de.gurkenlabs.litiengine.input;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.physics.MovementController;
import de.gurkenlabs.util.MathUtilities;

/**
 * TODO: Apply friction to terrain in order to slow down acceleration and speed
 * up deceleration.
 */
public class KeyboardEntityController<T extends IMovableEntity> extends MovementController<T> implements IKeyObserver {
  private float dx;
  private float dy;
  private boolean movedX;
  private boolean movedY;
  private final List<Integer> up;
  private final List<Integer> down;
  private final List<Integer> left;
  private final List<Integer> right;
  private double velocityX;
  private double velocityY;

  public KeyboardEntityController(final T entity) {
    this(entity, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D);
  }

  public KeyboardEntityController(final T entity, final int up, final int down, final int left, final int right) {
    super(entity);
    this.up = new ArrayList<>();
    this.down = new ArrayList<>();
    this.left = new ArrayList<>();
    this.right = new ArrayList<>();

    this.up.add(up);
    this.down.add(down);
    this.left.add(left);
    this.right.add(right);
    Input.keyboard().registerForKeyDownEvents(this);
  }

  @Override
  public void handlePressedKey(final KeyEvent keyCode) {

    if (this.up.contains(keyCode.getKeyCode())) {
      this.dy--;
      this.movedY = true;
    } else if (this.down.contains(keyCode.getKeyCode())) {
      this.movedY = true;
      this.dy++;
    } else if (this.left.contains(keyCode.getKeyCode())) {
      this.dx--;
      this.movedX = true;
    } else if (this.right.contains(keyCode.getKeyCode())) {
      this.dx++;
      this.movedX = true;
    }
  }

  public void addUpKey(int keyCode) {
    this.up.add(keyCode);
  }

  public void addDownKey(int keyCode) {
    this.down.add(keyCode);
  }

  public void addLeftKey(int keyCode) {
    this.left.add(keyCode);
  }

  public void addRightKey(int keyCode) {
    this.right.add(keyCode);
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
    if (!this.isMovementAllowed()) {
      return;
    }

    final long deltaTime = loop.getDeltaTime();

    // pixels per ms multiplied by the passed ms
    final double maxPixelsPerTick = this.getEntity().getVelocity() / 1000.0 * deltaTime;

    double accelerationRatio = (double) deltaTime / (double) this.getEntity().getAcceleration();
    double decelerationRatio = (double) deltaTime / (double) this.getEntity().getDeceleration();

    double inc = this.getEntity().getAcceleration() == 0 ? maxPixelsPerTick : accelerationRatio * maxPixelsPerTick;
    final double dec = this.getEntity().getDeceleration() == 0 ? maxPixelsPerTick : decelerationRatio * maxPixelsPerTick;
    final double STOP_THRESHOLD = 0.0025 * deltaTime;

    if (this.movedX && this.movedY) {
      // we don't want the entity to move faster when moving diagonally
      // calculate a new x by dissolving the formula for diagonals of squares
      // sqrt(2 * x^2)
      inc /= Math.sqrt(2);
    }

    if (this.movedX) {
      this.velocityX += this.dx > 0 ? inc : -inc;
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
      this.velocityY += this.dy > 0 ? inc : -inc;
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
}
