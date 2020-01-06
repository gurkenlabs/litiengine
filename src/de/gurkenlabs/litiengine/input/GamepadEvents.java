package de.gurkenlabs.litiengine.input;

import java.util.EventListener;

/**
 * The <code>GamepadEvents</code> interface is the engine's implementation for receiving gamepad input events.
 */
public interface GamepadEvents {

  /**
   * Adds the specified gamepad poll listener to receive events when the component with the defined identifier has been polled.
   * 
   * @param identifier
   *          The component identifier for which to add the listener.
   * @param listener
   *          The listener to add.
   */
  public void onPoll(String identifier, GamepadPollListener listener);

  /**
   * Unregister the specified poll listener from gamepad events.
   * 
   * @param identifier
   *          The component identifier for which to remove the listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removePollListener(String identifier, GamepadPollListener listener);

  /**
   * Adds the specified gamepad pressed listener to receive events when the component with the defined identifier has been pressed.
   * 
   * @param identifier
   *          The component identifier for which to add the listener.
   * @param listener
   *          The listener to add.
   */
  public void onPressed(String identifier, GamepadPressedListener listener);

  /**
   * Unregister the specified pressed listener from gamepad events.
   * 
   * @param identifier
   *          The component identifier for which to remove the listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removePressedListener(String identifier, GamepadPressedListener listener);

  /**
   * Adds the specified gamepad released listener to receive events when the component with the defined identifier has been released.
   * 
   * @param identifier
   *          The component identifier for which to add the listener.
   * @param listener
   *          The listener to add.
   */
  public void onReleased(String identifier, GamepadReleasedListener listener);

  /**
   * Unregister the specified released listener from gamepad events.
   * 
   * @param identifier
   *          The component identifier for which to remove the listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removeReleasedListener(String identifier, GamepadReleasedListener listener);

  /**
   * Adds the specified gamepad poll listener to receive events when any component has been polled.
   * 
   * @param listener
   *          The listener to add.
   */
  public void onPoll(GamepadPollListener listener);

  /**
   * Unregister the specified poll listener from gamepad events.
   *
   * @param listener
   *          The listener to remove.
   */
  public void removePollListener(GamepadPollListener listener);

  /**
   * Adds the specified gamepad pressed listener to receive events when any component has been pressed.
   * 
   * @param listener
   *          The listener to add.
   */
  public void onPressed(GamepadPressedListener listener);

  /**
   * Unregister the specified pressed listener from gamepad events.
   *
   * @param listener
   *          The listener to remove.
   */
  public void removePressedListener(GamepadPressedListener listener);

  /**
   * Adds the specified gamepad released listener to receive events when any component has been released.
   * 
   * @param listener
   *          The listener to add.
   */
  public void onReleased(GamepadReleasedListener listener);

  /**
   * Unregister the specified released listener from gamepad events.
   *
   * @param listener
   *          The listener to remove.
   */
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

  /**
   * This listener interface receives poll events for a gamepad.
   * 
   * @see GamepadEvents#onPoll(GamepadPollListener)
   * @see GamepadEvents#onPoll(String, GamepadPollListener)
   */
  @FunctionalInterface
  public interface GamepadPollListener extends EventListener {
    /**
     * Invoked when a gamepad component is being polled
     * 
     * @param event
     *          The gamepad event.
     */
    void polled(GamepadEvent event);
  }

  /**
   * This listener interface receives pressed events for a gamepad.
   * 
   * @see GamepadEvents#onPressed(GamepadPressedListener)
   * @see GamepadEvents#onPressed(String, GamepadPressedListener)
   */
  @FunctionalInterface
  public interface GamepadPressedListener extends EventListener {
    /**
     * Invoked when a gamepad component has been pressed.
     * 
     * @param event
     *          The gamepad event.
     */
    void pressed(GamepadEvent event);
  }

  /**
   * This listener interface receives released events for a gamepad.
   * 
   * @see GamepadEvents#onReleased(GamepadReleasedListener)
   * @see GamepadEvents#onReleased(String, GamepadReleasedListener)
   */
  @FunctionalInterface
  public interface GamepadReleasedListener extends EventListener {
    /**
     * Invoked when a gamepad component has been released.
     * 
     * @param event
     *          The gamepad event.
     */
    void released(GamepadEvent event);
  }
}
