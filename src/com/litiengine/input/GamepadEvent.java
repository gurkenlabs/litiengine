package com.litiengine.input;

import java.util.EventObject;

import net.java.games.input.Component;

public class GamepadEvent extends EventObject {
  private static final long serialVersionUID = -6174813700465768116L;

  private final transient Gamepad gamepad;
  private final String component;
  private final float value;
  private final boolean relative;
  private final boolean analog;

  GamepadEvent(Gamepad source, Component component) {
    super(source);
    this.gamepad = source;
    this.component = component.getName();
    this.value = component.getPollData();
    this.relative = component.isRelative();
    this.analog = component.isAnalog();
  }

  GamepadEvent(Gamepad source, String component, float value) {
    super(source);
    this.gamepad = source;
    this.component = component;
    this.value = value;
    this.relative = false;
    this.analog = false;
  }

  /**
   * Gets the data from the last time the component has been polled.
   * If this axis is a button, the value returned will be either 0.0f or 1.0f.
   * If this axis is normalized, the value returned will be between -1.0f and
   * 1.0f.
   * 
   * @return The last poll value of the component of this event.
   */
  public float getValue() {
    return this.value;
  }

  /**
   * Gets the identifier of the component that caused this event.
   * 
   * @return The human-readable name of the component.
   */
  public String getComponent() {
    return this.component;
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
   * Returns {@code true} if data returned from {@code poll}
   * is relative to the last call, or {@code false} if data
   * is absolute.
   * 
   * @return True if the data is relative; otherwise false.
   */
  public boolean isRelative() {
    return this.relative;
  }

  /**
   * Returns whether or not the axis is analog, or false if it is digital.
   * 
   * @return True if the component is analog; otherwise false.
   */
  public boolean isAnalog() {
    return this.analog;
  }
}
