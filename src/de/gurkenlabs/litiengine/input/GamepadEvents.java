package de.gurkenlabs.litiengine.input;

import java.util.EventListener;

public interface GamepadEvents {

  public void onPoll(String identifier, GamepadPollListener consumer);

  public void onPoll(GamepadPollListener consumer);

  public void onPressed(String identifier, GamepadPressedListener consumer);

  public void onPressed(GamepadPressedListener consumer);

  public void onReleased(String identifier, GamepadReleasedListener consumer);

  public void onReleased(GamepadReleasedListener consumer);

  /**
   * Removes all registered event consumers from the Gamepad instance.
   */
  public void clearEventConsumers();

  /**
   * Determines whether the specified Gamepad component is currently pressed.
   * This is useful for button type components.
   * 
   * @param gamepadComponent
   *          The component to check against.
   * @return True if the component is pressed, otherwise false.
   * 
   * @see Gamepad.Buttons
   * @see Gamepad.Xbox
   */
  public boolean isPressed(String gamepadComponent);

  @FunctionalInterface
  public interface GamepadPollListener extends EventListener {
    void polled(GamepadEvent event);
  }
  
  @FunctionalInterface
  public interface GamepadPressedListener extends EventListener {
    void pressed(GamepadEvent event);
  }
  
  @FunctionalInterface
  public interface GamepadReleasedListener extends EventListener {
    void released(GamepadEvent event);
  }
}
