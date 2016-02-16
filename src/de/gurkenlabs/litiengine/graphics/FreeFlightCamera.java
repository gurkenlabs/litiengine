package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;

public class FreeFlightCamera extends Camera {
  private Point2D location;

  public FreeFlightCamera(final Point2D location) {
    this.location = location;
  }

  @Override
  public double getCenterX() {
    return Game.getScreenManager().getResolution().width / 2.0 / Game.getInfo().renderScale();
  }

  @Override
  public double getCenterY() {
    return Game.getScreenManager().getResolution().height / 2.0 / Game.getInfo().renderScale();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getCameraRegion()
   */
  @Override
  public Rectangle2D getViewPort() {
    return new Rectangle2D.Double(this.getFocus().getX() - this.getCenterX(), this.getFocus().getY() - this.getCenterY(), Game.getScreenManager().getResolution().width / Game.getInfo().renderScale(), Game.getScreenManager().getResolution().height / Game.getInfo().renderScale());
  }

  @Override
  public void updateFocus() {
    this.setFocus(this.applyShakeEffect(this.location));
  }

  public void setLocation(Point2D location) {
    this.location = location;
  }

  public Point2D getLocation() {
    return this.location;
  }
}