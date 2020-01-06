package de.gurkenlabs.litiengine.input;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;

public class Keyboard implements KeyEventDispatcher, IKeyboard, IUpdateable {
  private final Collection<KeyListener> keyListeners = ConcurrentHashMap.newKeySet();
  private final Map<Integer, Collection<KeyPressedListener>> keySpecificPressedListener = new ConcurrentHashMap<>();
  private final Map<Integer, Collection<KeyReleasedListener>> keySpecificReleasedListener = new ConcurrentHashMap<>();
  private final Map<Integer, Collection<KeyTypedListener>> keySpecificTypedListener = new ConcurrentHashMap<>();
  private final Collection<KeyPressedListener> keyPressedListener = ConcurrentHashMap.newKeySet();
  private final Collection<KeyReleasedListener> keyReleasedListener = ConcurrentHashMap.newKeySet();
  private final Collection<KeyTypedListener> keyTypedListener = ConcurrentHashMap.newKeySet();

  private final Collection<KeyEvent> pressedKeys = ConcurrentHashMap.newKeySet();
  private final Collection<KeyEvent> releasedKeys = ConcurrentHashMap.newKeySet();
  private final Collection<KeyEvent> typedKeys = ConcurrentHashMap.newKeySet();

  private boolean consumeAlt;

  protected Keyboard() {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

    Game.inputLoop().attach(this);
    this.consumeAlt = true;
  }

  @Override
  public void consumeAlt(final boolean consume) {
    this.consumeAlt = consume;
  }

  @Override
  public boolean dispatchKeyEvent(final KeyEvent e) {
    if (this.consumeAlt && e.getKeyCode() == KeyEvent.VK_ALT) {
      e.consume();
    }
    final int eventId = e.getID();
    switch (eventId) {
    case KeyEvent.KEY_PRESSED:
      // on an avg. win 10 machine, this event fires every ~33 ms when a key is
      // pressed down
      this.addPressedKey(e);
      break;
    case KeyEvent.KEY_RELEASED:
      this.removePressedKey(e);
      this.addTypedKey(e);
      this.addReleasedKey(e);

      break;
    default:
      break;
    }

    return false;
  }

  @Override
  public boolean isPressed(final int keyCode) {
    for (final KeyEvent key : this.pressedKeys) {
      if (key.getKeyCode() == keyCode) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void onKeyPressed(final int keyCode, final KeyPressedListener listener) {
    this.keySpecificPressedListener.computeIfAbsent(keyCode, ConcurrentHashMap::newKeySet).add(listener);
  }

  @Override
  public void removeKeyPressedListener(int keyCode, KeyPressedListener listener) {
    if (!this.keySpecificPressedListener.containsKey(keyCode)) {
      return;
    }

    this.keySpecificPressedListener.get(keyCode).remove(listener);
  }

  @Override
  public void onKeyReleased(final int keyCode, final KeyReleasedListener listener) {
    this.keySpecificReleasedListener.computeIfAbsent(keyCode, ConcurrentHashMap::newKeySet).add(listener);
  }

  @Override
  public void removeKeyReleasedListener(int keyCode, KeyReleasedListener listener) {
    if (!this.keySpecificReleasedListener.containsKey(keyCode)) {
      return;
    }

    this.keySpecificReleasedListener.get(keyCode).remove(listener);
  }

  @Override
  public void onKeyTyped(final int keyCode, final KeyTypedListener listener) {
    this.keySpecificTypedListener.computeIfAbsent(keyCode, ConcurrentHashMap::newKeySet).add(listener);
  }

  @Override
  public void removeKeyTypedListener(int keyCode, KeyTypedListener listener) {
    if (!this.keySpecificTypedListener.containsKey(keyCode)) {
      return;
    }

    this.keySpecificTypedListener.get(keyCode).remove(listener);
  }

  @Override
  public void onKeyPressed(KeyPressedListener listener) {
    this.keyPressedListener.add(listener);
  }

  @Override
  public void removeKeyPressedListener(KeyPressedListener listener) {
    this.keyPressedListener.remove(listener);
  }

  @Override
  public void onKeyReleased(KeyReleasedListener listener) {
    this.keyReleasedListener.add(listener);
  }

  @Override
  public void removeKeyReleasedListener(KeyReleasedListener listener) {
    this.keyReleasedListener.remove(listener);
  }

  @Override
  public void onKeyTyped(KeyTypedListener listener) {
    this.keyTypedListener.add(listener);
  }

  @Override
  public void removeKeyTypedListener(KeyTypedListener listener) {
    this.keyTypedListener.remove(listener);
  }

  @Override
  public void clearExplicitListeners() {
    this.keyPressedListener.clear();
    this.keySpecificPressedListener.clear();

    this.keyReleasedListener.clear();
    this.keySpecificReleasedListener.clear();

    this.keyTypedListener.clear();
    this.keySpecificTypedListener.clear();
  }

  @Override
  public void addKeyListener(final KeyListener listener) {
    if (this.keyListeners.contains(listener)) {
      return;
    }

    this.keyListeners.add(listener);
  }

  @Override
  public void removeKeyListener(final KeyListener listener) {
    this.keyListeners.remove(listener);
  }

  @Override
  public void update() {
    this.executePressedKeys();
    this.executeReleasedKeys();
    this.executeTypedKeys();
  }

  /**
   * Adds the pressed key.
   *
   * @param keyCode
   *          the key code
   */
  private void addPressedKey(final KeyEvent keyCode) {
    if (this.pressedKeys.stream().anyMatch(key -> key.getKeyCode() == keyCode.getKeyCode())) {
      return;
    }

    this.pressedKeys.add(keyCode);
  }

  /**
   * Adds the released key.
   *
   * @param keyCode
   *          the key code
   */
  private void addReleasedKey(final KeyEvent keyCode) {
    if (this.releasedKeys.stream().anyMatch(key -> key.getKeyCode() == keyCode.getKeyCode())) {
      return;
    }

    this.releasedKeys.add(keyCode);
  }

  /**
   * Adds the typed key.
   *
   * @param keyCode
   *          the key code
   */
  private void addTypedKey(final KeyEvent keyCode) {
    if (this.typedKeys.stream().anyMatch(key -> key.getKeyCode() == keyCode.getKeyCode())) {
      return;
    }

    this.typedKeys.add(keyCode);
  }

  /**
   * Execute pressed keys.
   */
  private void executePressedKeys() {
    // called at the rate of the updaterate
    this.pressedKeys.forEach(key -> {
      this.keySpecificPressedListener.getOrDefault(key.getKeyCode(), Collections.emptySet()).forEach(listener -> listener.keyPressed(key));

      this.keyPressedListener.forEach(listener -> listener.keyPressed(key));
      this.keyListeners.forEach(listener -> listener.keyPressed(key));
    });
  }

  /**
   * Execute released keys.
   */
  private void executeReleasedKeys() {
    this.releasedKeys.forEach(key -> {
      this.keySpecificReleasedListener.getOrDefault(key.getKeyCode(), Collections.emptySet()).forEach(listener -> listener.keyReleased(key));

      this.keyReleasedListener.forEach(listener -> listener.keyReleased(key));
      this.keyListeners.forEach(listener -> listener.keyReleased(key));
    });

    this.releasedKeys.clear();
  }

  /**
   * Execute typed keys.
   */
  private void executeTypedKeys() {
    this.typedKeys.forEach(key -> {
      this.keySpecificTypedListener.getOrDefault(key.getKeyCode(), Collections.emptySet()).forEach(listener -> listener.keyTyped(key));

      this.keyTypedListener.forEach(listener -> listener.keyTyped(key));
      this.keyListeners.forEach(listener -> listener.keyTyped(key));
    });

    this.typedKeys.clear();
  }

  /**
   * Removes the pressed key.
   *
   * @param keyCode
   *          the key code
   */
  private void removePressedKey(final KeyEvent keyCode) {
    for (final KeyEvent removeKey : this.pressedKeys) {
      if (removeKey.getKeyCode() == keyCode.getKeyCode()) {
        this.pressedKeys.remove(removeKey);
        return;
      }
    }
  }
}