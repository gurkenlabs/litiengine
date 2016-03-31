package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;

public class FreeFlightCamera extends Camera {
  private Point2D location;

  public FreeFlightCamera(final Point2D location) {
    this.location = location;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getCameraRegion()
   */
  @Override
  public Rectangle2D getViewPort() {
    return new Rectangle2D.Double(this.getFocus().getX() - this.getCenterX(), this.getFocus().getY() - this.getCenterY(), Game.getScreenManager().getResolution().getWidth() / Game.getInfo().renderScale(), Game.getScreenManager().getResolution().getHeight() / Game.getInfo().renderScale());
  }

  @Override
  public void updateFocus() {
    this.setFocus(this.applyShakeEffect(this.location));
    this.setCenterX(Game.getScreenManager().getResolution().getWidth() * 0.5 / Game.getInfo().renderScale());
    this.setCenterY(Game.getScreenManager().getResolution().getHeight() * 0.5 / Game.getInfo().renderScale());
  }

  public void setLocation(final Point2D location) {
    this.location = location;
  }

  public Point2D getLocation() {
    return this.location;
  }
}