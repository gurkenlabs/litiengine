package de.gurkenlabs.litiengine.input;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IMovableEntity;

public class WASDEntityController extends ClientEntityMovementController implements IKeyObserver {
  private float stepSize;

  private boolean moved;
  private float dx;
  private float dy;

  public WASDEntityController(final IMovableEntity entity, final float stepSize) {
    super(entity);
    this.stepSize = stepSize;
    Input.KEYBOARD.registerForKeyDownEvents(this);
  }

  @Override
  public void update(final IGameLoop loop) {
    super.update(loop);

    if (this.moved) {
      Point2D newLocation = new Point2D.Double(this.getControlledEntity().getLocation().getX() + this.dx, this.getControlledEntity().getLocation().getY() + this.dy);
      Game.getPhysicsEngine().move(this.getControlledEntity(), newLocation, this.stepSize * this.getControlledEntity().getVelocity() / 100.0f);

      this.dx = 0;
      this.dy = 0;
      this.moved = false;
    }
  }

  @Override
  public void handlePressedKey(final int keyCode) {

    switch (keyCode) {
    case KeyEvent.VK_W:
      this.dy -= this.stepSize;
      break;
    case KeyEvent.VK_A:
      this.dx -= this.stepSize;
      break;
    case KeyEvent.VK_S:
      this.dy += this.stepSize;
      break;
    case KeyEvent.VK_D:
      this.dx += this.stepSize;
      break;
    }

    this.moved = true;
  }

  @Override
  public void handleReleasedKey(final int keyCode) {

  }

  @Override
  public void handleTypedKey(final int keyCode) {

  }

  public float getStepSize() {
    return this.stepSize;
  }

  public void setStepSize(final float stepSize) {
    this.stepSize = stepSize;
  }

}
