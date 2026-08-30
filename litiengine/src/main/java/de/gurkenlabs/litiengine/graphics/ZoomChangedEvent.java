package de.gurkenlabs.litiengine.graphics;

import java.io.Serial;

/// Event emitted after a camera's zoom factor changes.
///
/// @see ICamera#onZoom(ZoomChangedListener)
public class ZoomChangedEvent extends CameraEvent {
  @Serial private static final long serialVersionUID = -427566098748292912L;
  private final double zoom;

  ZoomChangedEvent(ICamera source, double zoom) {
    super(source);
    this.zoom = zoom;
  }

  /// Returns the new zoom factor.
  ///
  /// @return The new camera zoom.
  public double getZoom() {
    return this.zoom;
  }
}
