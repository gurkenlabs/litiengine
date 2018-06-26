package de.gurkenlabs.litiengine.input;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.physics.AccelerationMovementController;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import net.java.games.input.Component.Identifier;

public class GamepadEntityController<T extends IMobileEntity> extends AccelerationMovementController<T> {
  private int gamePadIndex = -1;
  private double gamePadDeadzone = Gamepad.STICK_DEADZONE;
  private double gamePadRightStick = Gamepad.STICK_DEADZONE;
  private boolean rotateWithRightStick = false;

  public GamepadEntityController(final T entity, boolean rotateWithRightStick) {
    super(entity);
    if (Input.getGamepad() != null) {
      this.gamePadIndex = Input.getGamepad().getIndex();
    }

    this.rotateWithRightStick = rotateWithRightStick;
    Input.gamepadManager().onGamepadAdded(pad -> {
      if (this.gamePadIndex == -1) {
        this.gamePadIndex = pad.getIndex();
      }
    });

    Input.gamepadManager().onGamepadRemoved(pad -> {
      if (this.gamePadIndex == pad.getIndex()) {
        this.gamePadIndex = -1;
        final Gamepad newGamePad = Input.getGamepad();
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

  public double getGamePadDeadzone() {
    return this.gamePadDeadzone;
  }

  public double getGamePadRightStick() {
    return gamePadRightStick;
  }

  public boolean isRotateWithRightStick() {
    return this.rotateWithRightStick;
  }

  public void setRightStickDeadzone(double gamePadRightStick) {
    this.gamePadRightStick = gamePadRightStick;
  }

  public void setLeftStickDeadzone(double gamePadDeadzone) {
    this.gamePadDeadzone = gamePadDeadzone;
  }

  public void setRotateWithRightStick(boolean rotateWithRightStick) {
    this.rotateWithRightStick = rotateWithRightStick;
  }

  private void retrieveGamepadValues() {
    if (this.gamePadIndex == -1 || this.gamePadIndex != -1 && Input.getGamepad(this.gamePadIndex) == null) {
      return;
    }

    final float x = Input.getGamepad(this.gamePadIndex).getPollData(Identifier.Axis.X);
    final float y = Input.getGamepad(this.gamePadIndex).getPollData(Identifier.Axis.Y);

    if (Math.abs(x) > this.gamePadDeadzone) {
      this.setDx(x);
      this.setMovedX(true);
    }

    if (Math.abs(y) > this.gamePadDeadzone) {
      this.setDy(y);
      this.setMovedY(true);
    }

    if (this.isRotateWithRightStick()) {
      final float rightX = Input.getGamepad(this.gamePadIndex).getPollData(Identifier.Axis.RX);
      final float rightY = Input.getGamepad(this.gamePadIndex).getPollData(Identifier.Axis.RY);
      float targetX = 0;
      float targetY = 0;
      if (Math.abs(rightX) > this.gamePadRightStick) {
        targetX = rightX;
      }
      if (Math.abs(rightY) > this.gamePadRightStick) {
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
