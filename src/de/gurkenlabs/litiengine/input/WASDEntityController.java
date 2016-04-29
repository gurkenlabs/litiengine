package de.gurkenlabs.litiengine.input;

import java.awt.event.KeyEvent;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.Direction;
import de.gurkenlabs.litiengine.entities.IMovableEntity;

public class WASDEntityController extends ClientEntityMovementController implements IKeyObserver {
  private float stepSize;

  // UP, LEFT, DOWN, RIGHT
  private final boolean[] pressed = new boolean[] { false, false, false, false };
  private boolean moved;

  public WASDEntityController(final IMovableEntity entity, final float stepSize) {
    super(entity);
    this.stepSize = stepSize;
    Input.KEYBOARD.registerForKeyDownEvents(this);
  }

  @Override
  public void update(final IGameLoop loop) {
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

      for (int i = 0; i < this.pressed.length; i++) {
        if (!this.pressed[i]) {
          continue;
        }

        this.pressed[i] = false;
        
        Direction dir = Direction.UNDEFINED;
        switch (i) {
        case 0:
          dir = Direction.UP;
          break;
        case 1:
          dir = Direction.LEFT;
          break;
        case 2:
          dir = Direction.DOWN;
          break;
        case 3:
          dir = Direction.RIGHT;
          break;
        }
        
        Game.getPhysicsEngine().move(this.getControlledEntity(), Direction.toAngle(dir), this.stepSize);
      }
      
      this.moved = false;
    }
  }

  @Override
  public void handlePressedKey(final int keyCode) {

    switch (keyCode) {
    case KeyEvent.VK_W:
      this.pressed[0] = true;
      break;
    case KeyEvent.VK_A:
      this.pressed[1] = true;
      break;
    case KeyEvent.VK_S:
      this.pressed[2] = true;
      break;
    case KeyEvent.VK_D:
      this.pressed[3] = true;
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
