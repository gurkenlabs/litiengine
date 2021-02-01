package com.litiengine.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code GamepadEvents} class is the engine's implementation for receiving gamepad input events.
 */
public abstract class GamepadEvents {
  protected final Map<String, Collection<GamepadPollListener>> componentPollListeners;
  protected final Map<String, Collection<GamepadPressedListener>> componentPressedListeners;
  protected final Map<String, Collection<GamepadReleasedListener>> componentReleasedListeners;
  protected final Collection<GamepadPollListener> pollListeners;
  protected final Collection<GamepadPressedListener> pressedListeners;
  protected final Collection<GamepadReleasedListener> releasedListeners;
  
  protected GamepadEvents() {
    this.componentPollListeners = new ConcurrentHashMap<>();
    this.componentPressedListeners = new ConcurrentHashMap<>();
    this.componentReleasedListeners = new ConcurrentHashMap<>();
    this.pollListeners = ConcurrentHashMap.newKeySet();
    this.pressedListeners = ConcurrentHashMap.newKeySet();
    this.releasedListeners = ConcurrentHashMap.newKeySet();
  }
  /**
   * Adds the specified gamepad poll listener to receive events when the component with the defined identifier has been polled.
   * 
   * @param identifier
   *          The component identifier for which to add the listener.
   * @param listener
   *          The listener to add.
   */
  public void onPoll(String identifier, GamepadPollListener listener){
    addComponentListener(this.componentPollListeners, identifier, listener);
  }

  /**
   * Unregister the specified poll listener from gamepad events.
   * 
   * @param identifier
   *          The component identifier for which to remove the listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removePollListener(String identifier, GamepadPollListener listener){
    removeComponentListener(this.componentPollListeners, identifier, listener);
  }

  /**
   * Adds the specified gamepad pressed listener to receive events when the component with the defined identifier has been pressed.
   * 
   * @param identifier
   *          The component identifier for which to add the listener.
   * @param listener
   *          The listener to add.
   */
  public void onPressed(String identifier, GamepadPressedListener listener){
    addComponentListener(this.componentPressedListeners, identifier, listener);
  }
  /**
   * Unregister the specified pressed listener from gamepad events.
   * 
   * @param identifier
   *          The component identifier for which to remove the listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removePressedListener(String identifier, GamepadPressedListener listener){
    removeComponentListener(this.componentPressedListeners, identifier, listener);
  }

  /**
   * Adds the specified gamepad released listener to receive events when the component with the defined identifier has been released.
   * 
   * @param identifier
   *          The component identifier for which to add the listener.
   * @param listener
   *          The listener to add.
   */
  public void onReleased(String identifier, GamepadReleasedListener listener){
    addComponentListener(this.componentReleasedListeners, identifier, listener);
  }

  /**
   * Unregister the specified released listener from gamepad events.
   * 
   * @param identifier
   *          The component identifier for which to remove the listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removeReleasedListener(String identifier, GamepadReleasedListener listener) {
    removeComponentListener(this.componentReleasedListeners, identifier, listener);
  }
  /**
   * Adds the specified gamepad poll listener to receive events when any component has been polled.
   * 
   * @param listener
   *          The listener to add.
   */
  public void onPoll(GamepadPollListener listener) {
    this.pollListeners.add(listener);
  }
  
  /**
   * Unregister the specified poll listener from gamepad events.
   *
   * @param listener
   *          The listener to remove.
   */
  public void removePollListener(GamepadPollListener listener){
    this.pollListeners.remove(listener);
  }
  
  /**
   * Adds the specified gamepad pressed listener to receive events when any component has been pressed.
   * 
   * @param listener
   *          The listener to add.
   */
  public void onPressed(GamepadPressedListener listener){
    this.pressedListeners.add(listener);
  }
  
  /**
   * Unregister the specified pressed listener from gamepad events.
   *
   * @param listener
   *          The listener to remove.
   */
  public void removePressedListener(GamepadPressedListener listener) {
    this.pressedListeners.remove(listener);
  }
  
  /**
   * Adds the specified gamepad released listener to receive events when any component has been released.
   * 
   * @param listener
   *          The listener to add.
   */
  public void onReleased(GamepadReleasedListener listener){
    this.releasedListeners.add(listener);
  }

  /**
   * Unregister the specified released listener from gamepad events.
   *
   * @param listener
   *          The listener to remove.
   */
  public void removeReleasedListener(GamepadReleasedListener listener){
    this.releasedListeners.remove(listener);
  }
  
  /**
   * Removes all registered event listeners from the Gamepad instance.
   */
  public void clearEventListeners() {
    this.componentPollListeners.clear();
    this.componentPressedListeners.clear();
    this.componentReleasedListeners.clear();

    this.pollListeners.clear();
    this.pressedListeners.clear();
    this.releasedListeners.clear();
  }
  
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
  public abstract boolean isPressed(String gamepadComponent);

  private static <T> void addComponentListener(Map<String, Collection<T>> consumerList, String identifier, T consumer) {
    if (!consumerList.containsKey(identifier)) {
      consumerList.put(identifier, new ArrayList<>());
    }

    consumerList.get(identifier).add(consumer);
  }

  private static <T> void removeComponentListener(Map<String, Collection<T>> consumerList, String identifier, T consumer) {
    if (!consumerList.containsKey(identifier)) {
      return;
    }

    consumerList.get(identifier).remove(consumer);
  }
  
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
