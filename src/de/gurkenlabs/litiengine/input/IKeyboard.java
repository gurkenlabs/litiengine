package de.gurkenlabs.litiengine.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EventListener;

public interface IKeyboard {

  public void consumeAlt(boolean consume);

  public boolean isPressed(int keyCode);

  public void onKeyPressed(int keyCode, KeyPressedListener consumer);

  public void removeKeyPressedListener(int keyCode, KeyPressedListener listener);

  public void onKeyReleased(int keyCode, KeyReleasedListener consumer);

  public void removeKeyReleasedListener(int keyCode, KeyReleasedListener listener);

  public void onKeyTyped(int keyCode, KeyTypedListener consumer);

  public void removeKeyTypedListener(int keyCode, KeyTypedListener listener);

  public void onKeyPressed(KeyPressedListener consumer);

  public void removeKeyPressedListener(KeyPressedListener listener);

  public void onKeyReleased(KeyReleasedListener consumer);

  public void removeKeyReleasedListener(KeyReleasedListener listener);

  public void onKeyTyped(KeyTypedListener consumer);

  public void removeKeyTypedListener(KeyTypedListener listener);

  /**
   * Removes all registered event consumers from the Keyboard instance. This <b>does not affect</b> registered <code>KeyListener</code> instances.
   * 
   * @see #onKeyPressed(KeyPressedListener)
   * @see #onKeyPressed(int, KeyPressedListener)
   * @see #onKeyReleased(KeyReleasedListener)
   * @see #onKeyReleased(int, KeyReleasedListener)
   * @see #onKeyTyped(KeyTypedListener)
   * @see #onKeyTyped(int, KeyTypedListener)
   */
  public void clearExplicitListeners();

  /**
   * Register for key events.
   *
   * @param listener
   *          The listener to add.
   */
  public void addKeyListener(KeyListener listener);

  /**
   * Unregister the specified listener from key events.
   *
   * @param listener
   *          The listener to remove.
   */
  public void removeKeyListener(KeyListener listener);

  @FunctionalInterface
  public interface KeyPressedListener extends EventListener {
    /**
     * Invoked when a key has been pressed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key pressed event.
     * 
     * @param event
     *          The key event.
     */
    void keyPressed(KeyEvent event);
  }

  @FunctionalInterface
  public interface KeyReleasedListener extends EventListener {
    /**
     * Invoked when a key has been released.
     * See the class description for {@link KeyEvent} for a definition of
     * a key released event.
     * 
     * @param event
     *          The key event.
     */
    void keyReleased(KeyEvent event);
  }

  @FunctionalInterface
  public interface KeyTypedListener extends EventListener {
    /**
     * Invoked when a key has been typed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key typed event.
     * 
     * @param event
     *          The key event.
     */
    void keyTyped(KeyEvent event);
  }
}
