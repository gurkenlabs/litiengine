package de.gurkenlabs.litiengine.input;

import java.awt.event.KeyEvent;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Direction;
import de.gurkenlabs.litiengine.entities.IMovableEntity;

public class WASDEntityController extends ClientEntityMovementController implements IKeyObserver {

  private final float stepSize;

  public WASDEntityController(final IMovableEntity entity, final float stepSize) {
    super(entity);
    this.stepSize = stepSize;
    Input.KEYBOARD.registerForKeyDownEvents(this);
  }

  @Override
  public void handlePressedKey(final int keyCode) {
    Direction dir = Direction.UNDEFINED;
    switch (keyCode) {
    case KeyEvent.VK_W:
      dir = Direction.UP;
      break;
    case KeyEvent.VK_A:
      dir = Direction.LEFT;
      break;
    case KeyEvent.VK_S:
      dir = Direction.DOWN;
      break;
    case KeyEvent.VK_D:
      dir = Direction.RIGHT;

      break;
    }

    if (dir != Direction.UNDEFINED) {
      Game.getPhysicsEngine().move(this.getControlledEntity(), Direction.toAngle(dir), this.stepSize);
    }
  }

  @Override
  public void handleReleasedKey(final int keyCode) {

  }

  @Override
  public void handleTypedKey(final int keyCode) {

  }
}
