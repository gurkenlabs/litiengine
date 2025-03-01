package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.input4j.InputComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code GamepadEvents} class is the engine's implementation for receiving gamepad input events.
 */
public abstract class GamepadEvents {
  protected final Map<InputComponent.ID, Collection<GamepadValueChangedListener>> componentPollListeners;
  protected final Map<InputComponent.ID, Collection<GamepadButtonPressedListener>> componentPressedListeners;
  protected final Map<InputComponent.ID, Collection<GamepadButtonReleasedListener>> componentReleasedListeners;
  protected final Collection<GamepadValueChangedListener> pollListeners;
  protected final Collection<GamepadButtonPressedListener> pressedListeners;
  protected final Collection<GamepadButtonReleasedListener> releasedListeners;

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
   * @param id       The component identifier for which to add the listener.
   * @param listener The listener to add.
   */
  public void onValueChanged(InputComponent.ID id, GamepadValueChangedListener listener) {
    addComponentListener(this.componentPollListeners, id, listener);
  }

  /**
   * Adds the specified gamepad pressed listener to receive events when the component with the defined identifier has been pressed.
   *
   * @param identifier The component identifier for which to add the listener.
   * @param listener   The listener to add.
   */
  public void onButtonPressed(InputComponent.ID identifier, GamepadButtonPressedListener listener) {
    addComponentListener(this.componentPressedListeners, identifier, listener);
  }

  /**
   * Adds the specified gamepad pressed listener to receive events when the component with the defined identifier has been pressed.
   *
   * @param id The component identifier for which to add the listener.
   * @param listener   The listener to add.
   */
  public void onButtonPressed(int id, GamepadButtonPressedListener listener){
    this.onButtonPressed(InputComponent.ID.getButton(id), listener);
  }

  /**
   * Adds the specified gamepad released listener to receive events when the component with the defined identifier has been released.
   *
   * @param identifier The component identifier for which to add the listener.
   * @param listener   The listener to add.
   */
  public void onButtonReleased(InputComponent.ID identifier, GamepadButtonReleasedListener listener) {
    addComponentListener(this.componentReleasedListeners, identifier, listener);
  }

  /**
   * Adds the specified gamepad released listener to receive events when the component with the defined identifier has been released.
   *
   * @param id The component identifier for which to add the listener.
   * @param listener   The listener to add.
   */
  public void onButtonReleased(int id, GamepadButtonReleasedListener listener){
    this.onButtonReleased(InputComponent.ID.getButton(id), listener);
  }

  /**
   * Adds the specified gamepad poll listener to receive events when any component has been polled.
   *
   * @param listener The listener to add.
   */
  public void onValueChanged(GamepadValueChangedListener listener) {
    this.pollListeners.add(listener);
  }

  /**
   * Adds the specified gamepad pressed listener to receive events when any component has been pressed.
   *
   * @param listener The listener to add.
   */
  public void onButtonPressed(GamepadButtonPressedListener listener) {
    this.pressedListeners.add(listener);
  }

  /**
   * Adds the specified gamepad released listener to receive events when any component has been released.
   *
   * @param listener The listener to add.
   */
  public void onButtonReleased(GamepadButtonReleasedListener listener) {
    this.releasedListeners.add(listener);
  }

  /**
   * Unregister the specified poll listener from gamepad events.
   *
   * @param identifier The component identifier for which to remove the listener.
   * @param listener   The listener to remove.
   */
  public void removePollListener(InputComponent.ID identifier, GamepadValueChangedListener listener) {
    removeComponentListener(this.componentPollListeners, identifier, listener);
  }


  /**
   * Unregister the specified pressed listener from gamepad events.
   *
   * @param identifier The component identifier for which to remove the listener.
   * @param listener   The listener to remove.
   */
  public void removeButtonPressedListener(InputComponent.ID identifier, GamepadButtonPressedListener listener) {
    removeComponentListener(this.componentPressedListeners, identifier, listener);
  }

  /**
   * Unregister the specified released listener from gamepad events.
   *
   * @param id The component identifier for which to remove the listener.
   * @param listener   The listener to remove.
   */
  public void removeButtonPressedListener(int id, GamepadButtonPressedListener listener){
    this.removeButtonPressedListener(InputComponent.ID.getButton(id), listener);
  }

  /**
   * Unregister the specified released listener from gamepad events.
   *
   * @param identifier The component identifier for which to remove the listener.
   * @param listener   The listener to remove.
   */
  public void removeButtonReleasedListener(InputComponent.ID identifier, GamepadButtonReleasedListener listener) {
    removeComponentListener(this.componentReleasedListeners, identifier, listener);
  }

  /**
   * Unregister the specified released listener from gamepad events.
   *
   * @param id The component identifier for which to remove the listener.
   * @param listener   The listener to remove.
   */
  public void removeButtonReleasedListener(int id, GamepadButtonReleasedListener listener){
    this.removeButtonReleasedListener(InputComponent.ID.getButton(id), listener);
  }

  /**
   * Unregister the specified poll listener from gamepad events.
   *
   * @param listener The listener to remove.
   */
  public void removePollListener(GamepadValueChangedListener listener) {
    this.pollListeners.remove(listener);
  }

  /**
   * Unregister the specified pressed listener from gamepad events.
   *
   * @param listener The listener to remove.
   */
  public void removeButtonPressedListener(GamepadButtonPressedListener listener) {
    this.pressedListeners.remove(listener);
  }

  /**
   * Unregister the specified released listener from gamepad events.
   *
   * @param listener The listener to remove.
   */
  public void removeButtonReleasedListener(GamepadButtonReleasedListener listener) {
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
   * Determines whether the specified Gamepad component is currently pressed. This is useful for button type components.
   *
   * @param buttonId The component to check against.
   * @return True if the component is pressed, otherwise false.
   */
  public abstract boolean isButtonPressed(InputComponent.ID buttonId);

  public abstract boolean isButtonPressed(int buttonId);

  private static <T> void addComponentListener(Map<InputComponent.ID, Collection<T>> consumerList, InputComponent.ID identifier, T consumer) {
    consumerList.putIfAbsent(identifier, new ArrayList<>());
    consumerList.get(identifier).add(consumer);
  }

  private static <T> void removeComponentListener(Map<InputComponent.ID, Collection<T>> consumerList, InputComponent.ID identifier, T consumer) {
    if (!consumerList.containsKey(identifier)) {
      return;
    }

    consumerList.get(identifier).remove(consumer);
  }

  /**
   * This listener interface receives poll events for a gamepad.
   *
   * @see GamepadEvents#onValueChanged(GamepadValueChangedListener)
   * @see GamepadEvents#onValueChanged(de.gurkenlabs.input4j.InputComponent.ID, GamepadValueChangedListener)
   */
  @FunctionalInterface
  public interface GamepadValueChangedListener extends EventListener {
    /**
     * Invoked when a gamepad component value was changed.
     *
     * @param event The gamepad event.
     */
    void valueChanged(GamepadEvent event);
  }

  /**
   * This listener interface receives pressed events for a gamepad.
   *
   * @see GamepadEvents#onButtonPressed(GamepadButtonPressedListener)
   * @see GamepadEvents#onButtonPressed(de.gurkenlabs.input4j.InputComponent.ID, GamepadButtonPressedListener)
   */
  @FunctionalInterface
  public interface GamepadButtonPressedListener extends EventListener {
    /**
     * Invoked when a gamepad component has been pressed.
     *
     * @param event The gamepad event.
     */
    void pressed(GamepadEvent event);
  }

  /**
   * This listener interface receives released events for a gamepad.
   *
   * @see GamepadEvents#onButtonReleased(GamepadButtonReleasedListener)
   * @see GamepadEvents#onButtonReleased(de.gurkenlabs.input4j.InputComponent.ID, GamepadButtonReleasedListener)
   */
  @FunctionalInterface
  public interface GamepadButtonReleasedListener extends EventListener {
    /**
     * Invoked when a gamepad component has been released.
     *
     * @param event The gamepad event.
     */
    void buttonReleased(GamepadEvent event);
  }
}
