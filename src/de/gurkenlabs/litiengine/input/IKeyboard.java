package de.gurkenlabs.litiengine.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.IUpdateable;

public interface IKeyboard extends IUpdateable {

  public void consumeAlt(boolean consume);

  public String getText(KeyEvent e);

  public boolean isPressed(int keyCode);

  public void onKeyPressed(int keyCode, Consumer<KeyEvent> consumer);

  public void onKeyReleased(int keyCode, Consumer<KeyEvent> consumer);

  public void onKeyTyped(int keyCode, Consumer<KeyEvent> consumer);

  public void onKeyPressed(Consumer<KeyEvent> consumer);

  public void onKeyReleased(Consumer<KeyEvent> consumer);

  public void onKeyTyped(Consumer<KeyEvent> consumer);

  /**
   * Removes all registered event consumers from the Keyboard instance. This <b>does not affect</b> registered <code>KeyListener</code> instances.
   * 
   * @see #onKeyPressed(Consumer)
   * @see #onKeyPressed(int, Consumer)
   * @see #onKeyReleased(Consumer)
   * @see #onKeyReleased(int, Consumer)
   * @see #onKeyTyped(Consumer)
   * @see #onKeyTyped(int, Consumer)
   */
  public void clearEventConsumers();

  /**
   * Register for key events.
   *
   * @param observer
   *          the observer
   */
  public void addKeyListener(KeyListener observer);

  /**
   * Unregister from key down events.
   *
   * @param observer
   *          the observer
   */
  public void removeKeyListener(KeyListener observer);
}
