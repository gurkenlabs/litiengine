package de.gurkenlabs.litiengine.input;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.physics.MovementController;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

public class GamepadEntityController<T extends IMobileEntity> extends MovementController<T> {
  private int gamePadIndex = -1;
  private double gamepadDeadzone = Game.config().input().getGamepadStickDeadzone();
  private double gamepadRightStick = Game.config().input().getGamepadStickDeadzone();
  private boolean rotateWithRightStick = false;

  public GamepadEntityController(final T entity, boolean rotateWithRightStick) {
    super(entity);
    if (Input.gamepads().current() != null) {
      this.gamePadIndex = Input.gamepads().current().getIndex();
    }

    this.rotateWithRightStick = rotateWithRightStick;
    Input.gamepads().onAdded(pad -> {
      if (this.gamePadIndex == -1) {
        this.gamePadIndex = pad.getIndex();
      }
    });

    Input.gamepads().onRemoved(pad -> {
      if (this.gamePadIndex == pad.getIndex()) {
        this.gamePadIndex = -1;
        final Gamepad newGamePad = Input.gamepads().current();
        if (newGamePad != null) {
          this.gamePadIndex = newGamePad.getIndex();
        }
      }
    });
  }

  @Override
  public void update() {

    this.retrieveGamepadValues();
    super.update();
  }

  public double getGamepadDeadzone() {
    return this.gamepadDeadzone;
  }

  public double getGamepadRightStick() {
    return gamepadRightStick;
  }

  public boolean isRotateWithRightStick() {
    return this.rotateWithRightStick;
  }

  public void setRightStickDeadzone(double gamePadRightStick) {
    this.gamepadRightStick = gamePadRightStick;
  }

  public void setLeftStickDeadzone(double gamePadDeadzone) {
    this.gamepadDeadzone = gamePadDeadzone;
  }

  public void setRotateWithRightStick(boolean rotateWithRightStick) {
    this.rotateWithRightStick = rotateWithRightStick;
  }

  private void retrieveGamepadValues() {
    if (this.gamePadIndex == -1 || this.gamePadIndex != -1 && Input.gamepads().get(this.gamePadIndex) == null) {
      return;
    }

    final float x = Input.gamepads().get(this.gamePadIndex).getPollData(Gamepad.Axis.X);
    final float y = Input.gamepads().get(this.gamePadIndex).getPollData(Gamepad.Axis.Y);

    if (Math.abs(x) > this.gamepadDeadzone) {
      this.setDx(x);
    }

    if (Math.abs(y) > this.gamepadDeadzone) {
      this.setDy(y);
    }

    if (this.isRotateWithRightStick()) {
      final float rightX = Input.gamepads().get(this.gamePadIndex).getPollData(Gamepad.Axis.RX);
      final float rightY = Input.gamepads().get(this.gamePadIndex).getPollData(Gamepad.Axis.RY);
      float targetX = 0;
      float targetY = 0;
      if (Math.abs(rightX) > this.gamepadRightStick) {
        targetX = rightX;
      }
      if (Math.abs(rightY) > this.gamepadRightStick) {
        targetY = rightY;
      }

      if (targetX != 0 || targetY != 0) {
        final Point2D target = new Point2D.Double(this.getEntity().getCenter().getX() + targetX, this.getEntity().getCenter().getY() + targetY);
        final double angle = GeometricUtilities.calcRotationAngleInDegrees(this.getEntity().getCenter(), target);
        this.getEntity().setAngle((float) angle);
      }
    }
  }
}
