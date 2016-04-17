package de.gurkenlabs.litiengine.input;

import java.awt.event.KeyEvent;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.Direction;
import de.gurkenlabs.litiengine.entities.IMovableEntity;

public class WASDEntityController extends ClientEntityMovementController implements IKeyObserver {
  private float stepSize;

  // UP, LEFT, DOWN, RIGHT
  private boolean[] pressed = new boolean[] { false, false, false, false };
  private boolean moved;

  public WASDEntityController(final IMovableEntity entity, final float stepSize) {
    super(entity);
    this.stepSize = stepSize;
    Input.KEYBOARD.registerForKeyDownEvents(this);
  }

  @Override
  public void update(IGameLoop loop) {
    super.update(loop);

    if (this.moved) {
      // UP-DOWN
      if (this.pressed[0] && this.pressed[2]) {
        this.pressed[0] = false;
        this.pressed[2] = false;
      }

      // LEFT-RIGHT
      if (this.pressed[1] && this.pressed[3]) {
        this.pressed[1] = false;
        this.pressed[3] = false;
      }

      // UP-LEFT
      if (this.pressed[0] && this.pressed[1]) {
        Game.getPhysicsEngine().move(this.getControlledEntity(), (Direction.toAngle(Direction.UP) + Direction.toAngle(Direction.LEFT)) / 2.0f, this.stepSize);
        this.pressed[0] = false;
        this.pressed[1] = false;
      }

      // UP-RIGHT
      if (this.pressed[0] && this.pressed[3]) {
        Game.getPhysicsEngine().move(this.getControlledEntity(), (Direction.toAngle(Direction.UP) + Direction.toAngle(Direction.RIGHT)) / 2.0f, this.stepSize);
        this.pressed[0] = false;
        this.pressed[3] = false;
      }

      // DOWN-LEFT
      if (this.pressed[2] && this.pressed[1]) {
        Game.getPhysicsEngine().move(this.getControlledEntity(), (Direction.toAngle(Direction.DOWN) + Direction.toAngle(Direction.LEFT)) / 2.0f, this.stepSize);
        this.pressed[2] = false;
        this.pressed[1] = false;
      }

      // DOWN-RIGHT
      if (this.pressed[2] && this.pressed[3]) {
        Game.getPhysicsEngine().move(this.getControlledEntity(), (Direction.toAngle(Direction.DOWN) + Direction.toAngle(Direction.RIGHT)) % 360 / 2.0f, this.stepSize);
        this.pressed[2] = false;
        this.pressed[3] = false;
      }

      for (int i = 0; i < pressed.length; i++) {
        if (!pressed[i]) {
          continue;
        }

        pressed[i] = false;

        switch (i) {
        case 0:
          Game.getPhysicsEngine().move(this.getControlledEntity(), Direction.toAngle(Direction.UP), this.stepSize);
          break;
        case 1:
          Game.getPhysicsEngine().move(this.getControlledEntity(), Direction.toAngle(Direction.LEFT), this.stepSize);
          break;
        case 2:
          Game.getPhysicsEngine().move(this.getControlledEntity(), Direction.toAngle(Direction.DOWN), this.stepSize);
          break;
        case 3:
          Game.getPhysicsEngine().move(this.getControlledEntity(), Direction.toAngle(Direction.RIGHT), this.stepSize);
          break;
        }
      }
      this.moved = false;
    }
  }

  @Override
  public void handlePressedKey(final int keyCode) {

    switch (keyCode) {
    case KeyEvent.VK_W:
      pressed[0] = true;
      break;
    case KeyEvent.VK_A:
      pressed[1] = true;
      break;
    case KeyEvent.VK_S:
      pressed[2] = true;
      break;
    case KeyEvent.VK_D:
      pressed[3] = true;
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

  public void setStepSize(float stepSize) {
    this.stepSize = stepSize;
  }

}
