/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.input;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.IGameLoop;

/**
 * The listener interface for receiving userKey events. The class that is
 * interested in processing a userKey event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addUserKeyListener<code> method. When the userKey event
 * occurs, that object's appropriate method is invoked.
 *
 * @see UserKeyEvent
 */
public class KeyBoard implements KeyEventDispatcher, IKeyboard {

  /** The key observers. */
  private final CopyOnWriteArrayList<IKeyObserver> keyObservers;

  /** The pressed keys. */
  private final CopyOnWriteArrayList<Integer> pressedKeys;

  /** The released keys. */
  private final CopyOnWriteArrayList<Integer> releasedKeys;

  /** The typed keys. */
  private final CopyOnWriteArrayList<Integer> typedKeys;

  /**
   * Instantiates a new key board.
   */
  public KeyBoard() {
    this.pressedKeys = new CopyOnWriteArrayList<>();
    this.releasedKeys = new CopyOnWriteArrayList<>();
    this.typedKeys = new CopyOnWriteArrayList<>();
    this.keyObservers = new CopyOnWriteArrayList<>();

    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.KeyEventDispatcher#dispatchKeyEvent(java.awt.event.KeyEvent)
   */
  @Override
  public boolean dispatchKeyEvent(final KeyEvent e) {
    final int eventId = e.getID();
    final int keyCode = e.getKeyCode();
    switch (eventId) {
    case KeyEvent.KEY_PRESSED:
      this.addPressedKey(keyCode);
      break;
    case KeyEvent.KEY_RELEASED:
      this.removePressedKey(keyCode);
      this.addTypedKey(keyCode);
      this.addReleasedKey(keyCode);
      break;
    default:
      break;
    }

    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.input.IKeyboard#registerForKeyDownEvents(de.gurkenlabs.
   * liti.input.IKeyObserver)
   */
  @Override
  public void registerForKeyDownEvents(final IKeyObserver observer) {
    if (this.keyObservers.contains(observer)) {
      return;
    }

    this.keyObservers.add(observer);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.input.IKeyboard#unregisterFromKeyDownEvents(de.
   * gurkenlabs.liti.input.IKeyObserver)
   */
  @Override
  public void unregisterFromKeyDownEvents(final IKeyObserver observer) {
    if (!this.keyObservers.contains(observer)) {
      return;
    }

    this.keyObservers.remove(observer);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.core.IUpdateable#update()
   */
  @Override
  public void update(final IGameLoop gameLoop) {
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
  private void addPressedKey(final int keyCode) {
    if (this.pressedKeys.stream().anyMatch(key -> key.equals(keyCode))) {
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
  private void addReleasedKey(final int keyCode) {
    if (this.releasedKeys.stream().anyMatch(key -> key.equals(keyCode))) {
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
  private void addTypedKey(final int keyCode) {
    if (this.typedKeys.stream().anyMatch(key -> key.equals(keyCode))) {
      return;
    }

    this.typedKeys.add(keyCode);
  }

  /**
   * Execute pressed keys.
   */
  private void executePressedKeys() {
    this.pressedKeys.forEach(key -> this.keyObservers.forEach(observer -> observer.handlePressedKey(key)));
  }

  /**
   * Execute released keys.
   */
  private void executeReleasedKeys() {
    this.releasedKeys.forEach(key -> this.keyObservers.forEach(observer -> observer.handleReleasedKey(key)));
    this.releasedKeys.forEach(key -> this.removeReleasedKey(key));

  }

  /**
   * Execute typed keys.
   */
  private void executeTypedKeys() {
    this.typedKeys.forEach(key -> this.keyObservers.forEach(observer -> observer.handleTypedKey(key)));
    this.typedKeys.forEach(key -> this.removeTypedKey(key));

  }

  /**
   * Removes the pressed key.
   *
   * @param keyCode
   *          the key code
   */
  private void removePressedKey(final int keyCode) {
    for (final Integer removeKey : this.pressedKeys) {
      if (removeKey.equals(keyCode)) {
        this.pressedKeys.remove(removeKey);
        return;
      }
    }
  }

  /**
   * Removes the released key.
   *
   * @param keyCode
   *          the key code
   */
  private void removeReleasedKey(final int keyCode) {
    for (final Integer removeKey : this.releasedKeys) {
      if (removeKey.equals(keyCode)) {
        this.releasedKeys.remove(removeKey);
        return;
      }
    }
  }

  /**
   * Removes the typed key.
   *
   * @param keyCode
   *          the key code
   */
  private void removeTypedKey(final int keyCode) {
    for (final Integer removeKey : this.typedKeys) {
      if (removeKey.equals(keyCode)) {
        this.typedKeys.remove(removeKey);
        return;
      }
    }
  }
}