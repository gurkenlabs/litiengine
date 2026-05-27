package de.gurkenlabs.litiengine.graphics;

import java.io.Serial;

public class ZoomChangedEvent extends CameraEvent {
  @Serial private static final long serialVersionUID = -427566098748292912L;
  private final double zoom;

  ZoomChangedEvent(ICamera source, double zoom) {
    super(source);
    this.zoom = zoom;
  }

  public double getZoom() {
    return this.zoom;
  }
}
