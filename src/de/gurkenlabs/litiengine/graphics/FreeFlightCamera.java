package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;

public class FreeFlightCamera extends Camera {
  private Point2D location;

  public FreeFlightCamera(final Point2D location) {
    this.location = location;
  }

  public Point2D getLocation() {
    return this.location;
  }

  public void setLocation(final Point2D location) {
    this.location = location;
  }

  @Override
  public void updateFocus() {
    this.setFocus(this.applyShakeEffect(this.location));
    super.updateFocus();
  }
}