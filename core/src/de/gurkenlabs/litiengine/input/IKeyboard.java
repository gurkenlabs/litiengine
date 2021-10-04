package de.gurkenlabs.litiengine.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EventListener;

/** The {@code IKeyboard} interface is the engine's API for receiving keyboard input events. */
public interface IKeyboard {

  /**
   * Specifies whether the engine should consume key events with the ALT modifier.
   *
   * <p>This is useful to prevent unintended behavior of the default key processing.
   *
   * @param consume True if the events with the ALT modifier should be consumed.
   * @see KeyEvent#consume()
   * @see KeyEvent#isAltDown()
   */
  void consumeAlt(boolean consume);

  /**
   * Checks whether the key with the specified {@code keyCode} is currently being pressed.
   *
   * @param keyCode The keyCode to check for.
   * @return True if the key with the specified code is currently pressed.
   * @see #onKeyPressed(KeyPressedListener)
   * @see #onKeyPressed(int, KeyPressedListener)
   */
  boolean isPressed(int keyCode);

  /**
   * Checks whether the key with the specified {@code keyCode} was recently released.
   *
   * @param keyCode The keyCode to check for.
   * @return True if the key with the specified code was recently released.
   * @see #onKeyReleased(KeyReleasedListener)
   * @see #onKeyReleased(int, KeyReleasedListener)
   */
  boolean wasReleased(int keyCode);

  /**
   * Adds the specified key pressed listener to receive events when the key with the defined {@code
   * keyCode} has been pressed.
   *
   * @param keyCode The keyCode to capture the key pressed event for.
   * @param listener The listener to add.
   * @see KeyListener#keyPressed(KeyEvent)
   * @see KeyEvent#KEY_PRESSED
   */
  void onKeyPressed(int keyCode, KeyPressedListener listener);

  /**
   * Unregister the specified listener from key pressed events.
   *
   * @param keyCode The keyCode for which to remove the listener.
   * @param listener The listener to remove.
   */
  void removeKeyPressedListener(int keyCode, KeyPressedListener listener);

  /**
   * Adds the specified key released listener to receive events when the key with the defined {@code
   * keyCode} has been released.
   *
   * @param keyCode The keyCode to capture the key released event for.
   * @param listener The listener to add.
   * @see KeyListener#keyReleased(KeyEvent)
   * @see KeyEvent#KEY_RELEASED
   */
  void onKeyReleased(int keyCode, KeyReleasedListener listener);

  /**
   * Unregister the specified listener from key released events.
   *
   * @param keyCode The keyCode for which to remove the listener.
   * @param listener The listener to remove.
   */
  void removeKeyReleasedListener(int keyCode, KeyReleasedListener listener);

  /**
   * Adds the specified key typed listener to receive events when the key with the defined {@code
   * keyCode} has been typed.
   *
   * @param keyCode The keyCode to capture the key typed event for.
   * @param listener The listener to add.
   * @see KeyListener#keyTyped(KeyEvent)
   * @see KeyEvent#KEY_TYPED
   */
  void onKeyTyped(int keyCode, KeyTypedListener listener);

  /**
   * Unregister the specified listener from key typed events.
   *
   * @param keyCode The keyCode for which to remove the listener.
   * @param listener The listener to remove.
   */
  void removeKeyTypedListener(int keyCode, KeyTypedListener listener);

  /**
   * Adds the specified key pressed listener to receive events when any key has been pressed.
   *
   * @param listener The listener to add.
   * @see KeyListener#keyPressed(KeyEvent)
   * @see KeyEvent#KEY_PRESSED
   */
  void onKeyPressed(KeyPressedListener listener);

  /**
   * Unregister the specified listener from key pressed events.
   *
   * @param listener The listener to remove.
   */
  void removeKeyPressedListener(KeyPressedListener listener);

  /**
   * Adds the specified key released listener to receive events when any key has been released.
   *
   * @param listener The listener to add.
   * @see KeyListener#keyReleased(KeyEvent)
   * @see KeyEvent#KEY_RELEASED
   */
  void onKeyReleased(KeyReleasedListener listener);

  /**
   * Unregister the specified listener from key released events.
   *
   * @param listener The listener to remove.
   */
  void removeKeyReleasedListener(KeyReleasedListener listener);

  /**
   * Adds the specified key typed listener to receive events when any key has been typed.
   *
   * @param listener The listener to add.
   * @see KeyListener#keyTyped(KeyEvent)
   * @see KeyEvent#KEY_TYPED
   */
  void onKeyTyped(KeyTypedListener listener);

  /**
   * Unregister the specified listener from key typed events.
   *
   * @param listener The listener to remove.
   */
  void removeKeyTypedListener(KeyTypedListener listener);

  /**
   * Removes all registered event consumers from the Keyboard instance. This <b>does not affect</b>
   * registered {@code KeyListener} instances.
   *
   * @see #onKeyPressed(KeyPressedListener)
   * @see #onKeyPressed(int, KeyPressedListener)
   * @see #onKeyReleased(KeyReleasedListener)
   * @see #onKeyReleased(int, KeyReleasedListener)
   * @see #onKeyTyped(KeyTypedListener)
   * @see #onKeyTyped(int, KeyTypedListener)
   */
  void clearExplicitListeners();

  /**
   * Register for key events.
   *
   * @param listener The listener to add.
   */
  void addKeyListener(KeyListener listener);

  /**
   * Unregister the specified listener from key events.
   *
   * @param listener The listener to remove.
   */
  void removeKeyListener(KeyListener listener);

  /**
   * This listener interface receives pressed events for the keyboard.
   *
   * @see IKeyboard#onKeyPressed(KeyPressedListener)
   * @see IKeyboard#onKeyPressed(int, KeyPressedListener)
   * @see KeyListener#keyPressed(KeyEvent)
   */
  @FunctionalInterface
  interface KeyPressedListener extends EventListener {
    /**
     * Invoked when a key has been pressed. See the class description for {@link KeyEvent} for a
     * definition of a key pressed event.
     *
     * @param event The key event.
     */
    void keyPressed(KeyEvent event);
  }

  /**
   * This listener interface receives released events for the keyboard.
   *
   * @see IKeyboard#onKeyReleased(KeyReleasedListener)
   * @see IKeyboard#onKeyReleased(int, KeyReleasedListener)
   * @see KeyListener#keyReleased(KeyEvent)
   */
  @FunctionalInterface
  interface KeyReleasedListener extends EventListener {
    /**
     * Invoked when a key has been released. See the class description for {@link KeyEvent} for a
     * definition of a key released event.
     *
     * @param event The key event.
     */
    void keyReleased(KeyEvent event);
  }

  /**
   * This listener interface receives typed events for the keyboard.
   *
   * @see IKeyboard#onKeyTyped(KeyTypedListener)
   * @see IKeyboard#onKeyTyped(int, KeyTypedListener)
   * @see KeyListener#keyTyped(KeyEvent)
   */
  @FunctionalInterface
  interface KeyTypedListener extends EventListener {
    /**
     * Invoked when a key has been typed. See the class description for {@link KeyEvent} for a
     * definition of a key typed event.
     *
     * @param event The key event.
     */
    void keyTyped(KeyEvent event);
  }
}
