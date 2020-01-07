package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;

public class FocusChangedEvent extends CameraEvent {
  private static final long serialVersionUID = -7066039797167626439L;
  private final transient Point2D focus;

  public FocusChangedEvent(ICamera source, Point2D focus) {
    super(source);
    this.focus = focus;
  }

  public Point2D getFocus() {
    return this.focus;
  }

}
