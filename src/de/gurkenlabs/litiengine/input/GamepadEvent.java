package de.gurkenlabs.litiengine.input;

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

  public float getValue() {
    return this.value;
  }

  public String getComponent() {
    return this.component;
  }

  public Gamepad getGamepad() {
    return this.gamepad;
  }

  /**
   * Returns <code>true</code> if data returned from <code>poll</code>
   * is relative to the last call, or <code>false</code> if data
   * is absolute.
   */
  public boolean isRelative() {
    return this.relative;
  }

  /**
   * Returns whether or not the axis is analog, or false if it is digital.
   */
  public boolean isAnalog() {
    return this.analog;
  }
}
