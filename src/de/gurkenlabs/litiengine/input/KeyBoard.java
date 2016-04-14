/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.input;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

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

  private final List<Map.Entry<Integer, Consumer<Integer>>> keyTypedConsumer;
  private final List<Map.Entry<Integer, Consumer<Integer>>> keyPressedConsumer;
  private final List<Map.Entry<Integer, Consumer<Integer>>> keyReleasedConsumer;

  /** The key observers. */
  private final List<IKeyObserver> keyObservers;

  /** The pressed keys. */
  private final List<Integer> pressedKeys;

  /** The released keys. */
  private final List<Integer> releasedKeys;

  /** The typed keys. */
  private final List<Integer> typedKeys;

  /**
   * Instantiates a new key board.
   */
  public KeyBoard() {
    this.keyTypedConsumer = new CopyOnWriteArrayList<>();
    this.keyPressedConsumer = new CopyOnWriteArrayList<>();
    this.keyReleasedConsumer = new CopyOnWriteArrayList<>();
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
    this.pressedKeys.forEach(key -> {
      this.keyPressedConsumer.forEach(consumer -> {
        if (consumer.getKey().intValue() == key) {
          consumer.getValue().accept(key);
        }
      });
      this.keyObservers.forEach(observer -> observer.handlePressedKey(key));
    });
  }

  /**
   * Execute released keys.
   */
  private void executeReleasedKeys() {
    this.releasedKeys.forEach(key -> {
      this.keyReleasedConsumer.forEach(consumer -> {
        if (consumer.getKey().intValue() == key) {
          consumer.getValue().accept(key);
        }
      });

      this.keyObservers.forEach(observer -> observer.handleReleasedKey(key));
    });

    this.releasedKeys.clear();
  }

  /**
   * Execute typed keys.
   */
  private void executeTypedKeys() {
    this.typedKeys.forEach(key -> {
      this.keyTypedConsumer.forEach(consumer -> {
        if (consumer.getKey().intValue() == key) {
          consumer.getValue().accept(key);
        }
      });

      this.keyObservers.forEach(observer -> observer.handleTypedKey(key));
    });

    this.typedKeys.clear();
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

  @Override
  public void onKeyTyped(int keyCode, Consumer<Integer> consumer) {
    this.keyTypedConsumer.add(new AbstractMap.SimpleEntry<>(keyCode, consumer));
  }

  @Override
  public void onKeyReleased(int keyCode, Consumer<Integer> consumer) {
    this.keyReleasedConsumer.add(new AbstractMap.SimpleEntry<>(keyCode, consumer));
  }

  @Override
  public void onKeyPressed(int keyCode, Consumer<Integer> consumer) {
    this.keyPressedConsumer.add(new AbstractMap.SimpleEntry<>(keyCode, consumer));
  }
}