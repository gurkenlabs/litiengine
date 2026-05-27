package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;
import java.io.Serial;

public class FocusChangedEvent extends CameraEvent {
  @Serial private static final long serialVersionUID = -7066039797167626439L;
  private final transient Point2D focus;

  FocusChangedEvent(ICamera source, Point2D focus) {
    super(source);
    this.focus = focus;
  }

  public Point2D getFocus() {
    return this.focus;
  }
}
