package de.gurkenlabs.litiengine.input;

import java.util.EventListener;

public interface GamepadEvents {

  public void onPoll(String identifier, GamepadPollListener listener);

  public void removePollListener(String identifier, GamepadPollListener listener);

  public void onPressed(String identifier, GamepadPressedListener listener);

  public void removePressedListener(String identifier, GamepadPressedListener listener);

  public void onReleased(String identifier, GamepadReleasedListener listener);

  public void removeReleasedListener(String identifier, GamepadReleasedListener listener);

  public void onPoll(GamepadPollListener listener);

  public void removePollListener(GamepadPollListener listener);

  public void onPressed(GamepadPressedListener listener);

  public void removePressedListener(GamepadPressedListener listener);

  public void onReleased(GamepadReleasedListener listener);

  public void removeReleasedListener(GamepadReleasedListener listener);

  /**
   * Removes all registered event listeners from the Gamepad instance.
   */
  public void clearEventListeners();

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
