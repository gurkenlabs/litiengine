package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.InputDevicePlugin;
import de.gurkenlabs.input4j.InputDevices;

import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The {@code GamepadManager} provides access to all gamepad input devices.
 *
 * <p>
 * Gamepads don't need to be added explicitly, the manager supports hot-plugging at runtime and will auto-detect any
 * added/removed gamepads.
 *
 * @see #current()
 * @see #get(int)
 */
public final class GamepadManager extends GamepadEvents {
  private final Collection<GamepadAddedListener> gamepadAddedConsumer;
  private final Collection<GamepadRemovedListener> gamepadRemovedConsumer;
  private final InputDevicePlugin devicePlugin;
  private final List<Gamepad> gamePads;

  private String defaultgamepadId = null;

  GamepadManager() {
    this.gamepadRemovedConsumer = ConcurrentHashMap.newKeySet();
    this.gamepadAddedConsumer = ConcurrentHashMap.newKeySet();

    this.gamePads = new CopyOnWriteArrayList<>();
    this.devicePlugin = InputDevices.init();

    // initially add all gamepads before subscribing the events
    this.devicePlugin.getAll().forEach(this::addNewGamepad);

    // add new gamepads as they are connected
    this.devicePlugin.onDeviceConnected(device ->
    {
      final var gamepad = this.getById(device.getID());
      if (gamepad != null) {
        // already added
        return;
      }

      this.addNewGamepad(device);
    });

    // remove gamepads as they are disconnected
    this.devicePlugin.onDeviceDisconnected(device ->
    {
      final var gamepad = this.getById(device.getID());
      if (gamepad == null) {
        // gamepad was not added before, nothing to do
        return;
      }

      this.removeGamepad(gamepad);
    });
  }

  private void addNewGamepad(InputDevice device) {
    // add new gamepads
    final Gamepad newGamepad = new Gamepad(device);
    this.gamePads.add(newGamepad);
    if (this.defaultgamepadId == null) {
      this.defaultgamepadId = newGamepad.getId();
      this.hookupToGamepad(newGamepad);
    }

    for (final GamepadAddedListener listener : this.gamepadAddedConsumer) {
      listener.added(newGamepad);
    }
  }

  /**
   * Adds the specified gamepad added listener to receive events when gamepads are added.
   *
   * @param listener The listener to add.
   */
  public void onAdded(final GamepadAddedListener listener) {
    this.gamepadAddedConsumer.add(listener);
  }

  /**
   * Unregister the specified added listener from this instance.
   *
   * @param listener The listener to remove.
   */
  public void removeAddedListener(GamepadAddedListener listener) {
    this.gamepadAddedConsumer.remove(listener);
  }

  /**
   * Adds the specified gamepad removed listener to receive events when gamepads are removed.
   *
   * @param listener The listener to add.
   */
  public void onRemoved(final GamepadRemovedListener listener) {
    this.gamepadRemovedConsumer.add(listener);
  }

  /**
   * Unregister the specified removed listener from this instance.
   *
   * @param listener The listener to remove.
   */
  public void removeRemovedListener(GamepadRemovedListener listener) {
    this.gamepadRemovedConsumer.remove(listener);
  }

  /**
   * Gets all gamepads that are currently available.
   *
   * @return All available gamepads.
   * @see #get(int)
   * @see #current()
   */
  public List<Gamepad> getAll() {
    return this.gamePads;
  }

  /**
   * Gets the first gamepad that is currently available.
   *
   * @return The first available {@link Gamepad} instance
   * @see #get(int)
   * @see #getAll()
   */
  public Gamepad current() {
    return get(0);
  }

  /**
   * Gets the gamepad by the index within the gamepad list.
   *
   * @param index The index of the {@link Gamepad}.
   * @return The {@link Gamepad} with the specified index.
   * @see #getAll()
   * @see #current()
   */
  public Gamepad get(final int index) {
    if (this.gamePads.isEmpty()) {
      return null;
    }

    return this.gamePads.get(index);
  }

  /**
   * Gets the gamepad with the specified id if it is still plugged in. After re-plugging a controller while the game is
   * running, its id might change.
   *
   * @param id The id of the {@link Gamepad}.
   * @return The {@link Gamepad} with the specified index.
   * @see #getAll()
   * @see #current()
   */
  public Gamepad getById(final String id) {
    for (final Gamepad gamepad : this.gamePads) {
      if (gamepad.getId().equals(id)) {
        return gamepad;
      }
    }

    return null;
  }

  @Override
  public void onValueChanged(GamepadValueChangedListener listener) {
    super.onValueChanged(listener);
    final var current = this.current();
    if (current != null) {
      current.onValueChanged(listener);
    }
  }

  @Override
  public void onButtonPressed(GamepadButtonPressedListener listener) {
    super.onButtonPressed(listener);
    final var current = this.current();
    if (current != null) {
      current.onButtonPressed(listener);
    }
  }

  @Override
  public void onButtonReleased(GamepadButtonReleasedListener listener) {
    super.onButtonReleased(listener);
    final var current = this.current();
    if (current != null) {
      current.onButtonReleased(listener);
    }
  }

  @Override
  public void onValueChanged(InputComponent.ID identifier, GamepadValueChangedListener listener) {
    super.onValueChanged(identifier, listener);
    final var current = this.current();
    if (current != null) {
      current.onValueChanged(identifier, listener);
    }
  }

  @Override
  public void onButtonPressed(InputComponent.ID identifier, GamepadButtonPressedListener listener) {
    super.onButtonPressed(identifier, listener);

    final var current = this.current();
    if (current != null) {
      current.onButtonPressed(identifier, listener);
    }
  }

  @Override
  public void onButtonReleased(InputComponent.ID identifier, GamepadButtonReleasedListener listener) {
    super.onButtonReleased(identifier, listener);
    final var current = this.current();
    if (current != null) {
      current.onButtonReleased(identifier, listener);
    }
  }

  @Override
  public void clearEventListeners() {
    super.clearEventListeners();

    final var current = this.current();
    if (current != null) {
      current.clearEventListeners();
    }
  }

  @Override
  public boolean isButtonPressed(InputComponent.ID buttonId) {
    final Gamepad current = this.current();
    return current != null && current.isButtonPressed(buttonId);
  }

  @Override
  public boolean isButtonPressed(int buttonId) {
    return this.isButtonPressed(InputComponent.ID.getButton(buttonId));
  }

  @Override
  public void removePollListener(InputComponent.ID identifier, GamepadValueChangedListener listener) {
    super.removePollListener(identifier, listener);

    final var current = this.current();
    if (current != null) {
      current.removePollListener(identifier, listener);
    }
  }

  @Override
  public void removeButtonPressedListener(InputComponent.ID identifier, GamepadButtonPressedListener listener) {
    super.removeButtonPressedListener(identifier, listener);

    final var current = this.current();
    if (current != null) {
      current.removeButtonPressedListener(identifier, listener);
    }
  }

  @Override
  public void removeButtonReleasedListener(InputComponent.ID identifier, GamepadButtonReleasedListener listener) {
    super.removeButtonReleasedListener(identifier, listener);

    final var current = this.current();
    if (current != null) {
      current.removeButtonReleasedListener(identifier, listener);
    }
  }

  @Override
  public void removePollListener(GamepadValueChangedListener listener) {
    super.removePollListener(listener);

    final var current = this.current();
    if (current != null) {
      current.removePollListener(listener);
    }
  }

  @Override
  public void removeButtonPressedListener(GamepadButtonPressedListener listener) {
    super.removeButtonPressedListener(listener);

    final var current = this.current();
    if (current != null) {
      current.removeButtonPressedListener(listener);
    }
  }

  @Override
  public void removeButtonReleasedListener(GamepadButtonReleasedListener listener) {
    super.removeButtonReleasedListener(listener);

    final var current = this.current();
    if (current != null) {
      current.removeButtonReleasedListener(listener);
    }
  }

  private void removeGamepad(final Gamepad gamepad) {
    if (gamepad == null) {
      return;
    }

    gamepad.clearEventListeners();

    // reset default gamepad in case the removed one is the default
    if (this.defaultgamepadId.equals(gamepad.getId())) {
      this.defaultgamepadId = null;
      final var newGamePad = current();
      if (newGamePad != null) {
        this.defaultgamepadId = newGamePad.getId();
        this.hookupToGamepad(newGamePad);
      }
    }

    this.gamePads.remove(gamepad);
    for (final var listener : this.gamepadRemovedConsumer) {
      listener.removed(gamepad);
    }
  }

  private void hookupToGamepad(final Gamepad pad) {
    for (final var entry : this.componentPollListeners.entrySet()) {
      for (final GamepadValueChangedListener listener : entry.getValue()) {
        pad.onValueChanged(entry.getKey(), listener);
      }
    }

    for (final var entry : this.componentPressedListeners.entrySet()) {
      for (final GamepadButtonPressedListener listener : entry.getValue()) {
        pad.onButtonPressed(entry.getKey(), listener);
      }
    }

    for (final var entry : this.componentReleasedListeners.entrySet()) {
      for (final GamepadButtonReleasedListener listener : entry.getValue()) {
        pad.onButtonReleased(entry.getKey(), listener);
      }
    }

    for (final GamepadValueChangedListener listener : this.pollListeners) {
      pad.onValueChanged(listener);
    }

    for (final GamepadButtonPressedListener listener : this.pressedListeners) {
      pad.onButtonPressed(listener);
    }

    for (final GamepadButtonReleasedListener listener : this.releasedListeners) {
      pad.onButtonReleased(listener);
    }
  }

  /**
   * This listener interface receives events when gamepads gets added.
   *
   * @see GamepadManager#onAdded(GamepadAddedListener)
   */
  @FunctionalInterface
  public interface GamepadAddedListener extends EventListener {
    /**
     * Invoked when a gamepad was added.
     *
     * @param gamepad The added gamepad.
     */
    void added(Gamepad gamepad);
  }

  /**
   * This listener interface receives events when gamepads gets removed.
   *
   * @see GamepadManager#onAdded(GamepadAddedListener)
   */
  @FunctionalInterface
  public interface GamepadRemovedListener extends EventListener {
    /**
     * Invoked when a gamepad was removed.
     *
     * @param gamepad The removed gamepad.
     */
    void removed(Gamepad gamepad);
  }
}
