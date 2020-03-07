package de.gurkenlabs.litiengine.graphics;

import java.util.EventObject;

public abstract class CameraEvent extends EventObject {
  private static final long serialVersionUID = 6376977651819216179L;
  private final transient ICamera camera;

  CameraEvent(ICamera source) {
    super(source);
    this.camera = source;
  }

  public ICamera getCamera() {
    return this.camera;
  }
}
