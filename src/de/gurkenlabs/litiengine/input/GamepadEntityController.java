package de.gurkenlabs.litiengine.input;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.physics.AccelerationMovementController;
import de.gurkenlabs.util.geom.GeometricUtilities;
import net.java.games.input.Component.Identifier;

public class GamepadEntityController<T extends IMovableEntity> extends AccelerationMovementController<T> {
  private int gamePadIndex = -1;

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

    this.retrieveGamepadValues();
    super.update(loop);
  }

  private void retrieveGamepadValues() {
    if (this.gamePadIndex == -1 || this.gamePadIndex != -1 && Input.getGamepad(this.gamePadIndex) == null) {
      return;
    }

    final float x = Input.getGamepad(this.gamePadIndex).getPollData(Identifier.Axis.X);
    final float y = Input.getGamepad(this.gamePadIndex).getPollData(Identifier.Axis.Y);

    if (Math.abs(x) > 0.15) {
      this.setDx(x);
      this.setMovedX(true);
    }

    if (Math.abs(y) > 0.15) {
      this.setDy(y);
      this.setMovedY(true);
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
