package de.gurkenlabs.litiengine.graphics;

import java.awt.Point;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.input.Input;

public class FreeFlightCamera extends Camera implements IUpdateable{
  private Point2D location;

  public FreeFlightCamera(final Point2D location) {
    this.location = location;
    Game.getLoop().registerForUpdate(this);
  }
  
  @Override
  public void update(IGameLoop loop) {
    this.handleFreeFlightCamera(loop);
  }
  
  public Point2D getLocation() {
    return this.location;
  }

  public void setLocation(final Point2D location) {
    this.location = location;
  }

  @Override
  public void updateFocus() {
    this.setFocus(this.location);
    super.updateFocus();
  }
  
  private void handleFreeFlightCamera(final IGameLoop gameLoop) {
    final int SCROLL_PIXELS_PER_SECOND = 400;
    final int SCROLL_PADDING = 20;
    final int SCROLL_BORDER = 100;
    final Point mouseLocation = Input.MOUSE.getLocation();

    final double scrollSpeed = SCROLL_PIXELS_PER_SECOND / gameLoop.getUpdateRate() * Game.getConfiguration().INPUT.getMouseSensitivity();

    double x = this.getLocation().getX();
    double y = this.getLocation().getY();
    if (Math.abs(mouseLocation.x) < SCROLL_PADDING) {
      x -= scrollSpeed;
    } else if (Math.abs(Game.getScreenManager().getResolution().getWidth() - mouseLocation.x) < SCROLL_PADDING) {
      x += scrollSpeed;
    }

    if (Math.abs(mouseLocation.y) < SCROLL_PADDING) {
      y -= scrollSpeed;
    } else if (Math.abs(Game.getScreenManager().getResolution().getHeight() - mouseLocation.y) < SCROLL_PADDING) {
      y += scrollSpeed;
    }

    if (Game.getEnvironment() != null && Game.getEnvironment().getMap() != null) {
      // ensure that x is within the scroll border
      if (x < -SCROLL_BORDER || x > Game.getEnvironment().getMap().getSizeInPixels().getWidth() + SCROLL_BORDER) {
        x = this.getLocation().getX();
      }

      // ensure that y is within the scroll border
      if (y < -SCROLL_BORDER || y > Game.getEnvironment().getMap().getSizeInPixels().getHeight() + SCROLL_BORDER) {
        y = this.getLocation().getY();
      }
    }

    this.setLocation(new Point2D.Double(x, y));
  }

}