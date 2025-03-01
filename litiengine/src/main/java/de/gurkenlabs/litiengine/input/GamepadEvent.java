package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.input4j.InputComponent;

import java.util.EventObject;

public class GamepadEvent extends EventObject {
  private static final long serialVersionUID = -6174813700465768116L;

  private final transient Gamepad gamepad;
  private final String component;
  private final String componentName;
  private final float value;
  private final boolean relative;

  GamepadEvent(Gamepad source, InputComponent component) {
    super(source);
    this.gamepad = source;
    this.component = component.getType() + " " + component.getId().id;
    this.componentName = component.getId().name;
    this.value = component.getData();
    this.relative = component.isRelative();
  }

  /**
   * Gets the data from the last time the component has been polled. If this axis is a button, the value returned will be
   * either 0.0f or 1.0f. If this axis is normalized, the value returned will be between -1.0f and 1.0f.
   *
   * @return The last poll value of the component of this event.
   */
  public float getValue() {
    return this.value;
  }

  /**
   * Gets the identifier of the component that caused this event.
   *
   * @return The identifier of the component.
   */
  public String getComponentId() {
    return this.component;
  }

  /**
   * Gets the name of the component that caused this event.
   *
   * @return The human-readable name of the component.
   */
  public String getComponentName() {
    return this.componentName;
  }

  /**
   * Gets the gamepad that caused the event.
   *
   * @return The gamepad of this event.
   */
  public Gamepad getGamepad() {
    return this.gamepad;
  }

  /**
   * Returns {@code true} if data returned from {@code poll} is relative to the last call, or {@code
   * false} if data is absolute.
   *
   * @return True if the data is relative; otherwise false.
   */
  public boolean isRelative() {
    return this.relative;
  }
}
