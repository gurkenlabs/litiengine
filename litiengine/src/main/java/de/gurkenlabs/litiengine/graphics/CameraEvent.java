package de.gurkenlabs.litiengine.graphics;

import java.util.EventObject;

/**
 * Represents an abstract event related to the camera. This class serves as the base for all camera-related events in the game engine.
 */
public abstract class CameraEvent extends EventObject {
  private static final long serialVersionUID = 6376977651819216179L;

  /**
   * The camera associated with this event. This field is transient to avoid serialization issues.
   */
  private final transient ICamera camera;

  /**
   * Initializes a new instance of the {@code CameraEvent} class.
   *
   * @param source The camera that is the source of this event.
   */
  CameraEvent(ICamera source) {
    super(source);
    this.camera = source;
  }

  /**
   * Gets the camera associated with this event.
   *
   * @return The {@code ICamera} instance that triggered this event.
   */
  public ICamera getCamera() {
    return this.camera;
  }
}
