package de.gurkenlabs.litiengine.input;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;

public class Keyboard implements KeyEventDispatcher, IKeyboard {
  private final Collection<KeyListener> keyListeners = ConcurrentHashMap.newKeySet();
  private final Map<Integer, Collection<Consumer<KeyEvent>>> keySpecificPressedConsumer = new ConcurrentHashMap<>();
  private final Map<Integer, Collection<Consumer<KeyEvent>>> keySpecificReleasedConsumer = new ConcurrentHashMap<>();
  private final Map<Integer, Collection<Consumer<KeyEvent>>> keySpecificTypedConsumer = new ConcurrentHashMap<>();
  private final Collection<Consumer<KeyEvent>> keyPressedConsumer = ConcurrentHashMap.newKeySet();
  private final Collection<Consumer<KeyEvent>> keyReleasedConsumer = ConcurrentHashMap.newKeySet();
  private final Collection<Consumer<KeyEvent>> keyTypedConsumer = ConcurrentHashMap.newKeySet();

  private final List<KeyEvent> pressedKeys = new CopyOnWriteArrayList<>();
  private final List<KeyEvent> releasedKeys = new CopyOnWriteArrayList<>();
  private final List<KeyEvent> typedKeys = new CopyOnWriteArrayList<>();

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
  public String getText(final KeyEvent e) {
    if (this.isPressed(KeyEvent.VK_SHIFT) || Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK)) {
      return getShiftText(e);
    } else if (this.isPressed(KeyEvent.VK_ALT_GRAPH)) {
      return getAltText(e);
    } else {
      return getNormalText(e);
    }
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
  public void onKeyPressed(final int keyCode, final Consumer<KeyEvent> consumer) {
    this.keySpecificPressedConsumer.computeIfAbsent(keyCode, ConcurrentHashMap::newKeySet).add(consumer);
  }

  @Override
  public void onKeyReleased(final int keyCode, final Consumer<KeyEvent> consumer) {
    this.keySpecificReleasedConsumer.computeIfAbsent(keyCode, ConcurrentHashMap::newKeySet).add(consumer);
  }

  @Override
  public void onKeyTyped(final int keyCode, final Consumer<KeyEvent> consumer) {
    this.keySpecificTypedConsumer.computeIfAbsent(keyCode, ConcurrentHashMap::newKeySet).add(consumer);
  }

  @Override
  public void onKeyPressed(Consumer<KeyEvent> consumer) {
    this.keyPressedConsumer.add(consumer);
  }

  @Override
  public void onKeyReleased(Consumer<KeyEvent> consumer) {
    this.keyReleasedConsumer.add(consumer);
  }

  @Override
  public void onKeyTyped(Consumer<KeyEvent> consumer) {
    this.keyTypedConsumer.add(consumer);
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
      if (this.keySpecificPressedConsumer.get(key.getKeyCode()) != null) {
        this.keySpecificPressedConsumer.get(key.getKeyCode()).forEach(consumer -> consumer.accept(key));

        this.keyPressedConsumer.forEach(consumer -> consumer.accept(key));
        this.keyListeners.forEach(listener -> listener.keyPressed(key));
      }
    });
  }

  /**
   * Execute released keys.
   */
  private void executeReleasedKeys() {
    this.releasedKeys.forEach(key -> {
      if (this.keySpecificReleasedConsumer.get(key.getKeyCode()) != null) {
        this.keySpecificReleasedConsumer.get(key.getKeyCode()).forEach(consumer -> consumer.accept(key));

        this.keyReleasedConsumer.forEach(consumer -> consumer.accept(key));
        this.keyListeners.forEach(listener -> listener.keyReleased(key));
      }
    });

    this.releasedKeys.clear();
  }

  /**
   * Execute typed keys.
   */
  private void executeTypedKeys() {
    this.typedKeys.forEach(key -> {
      if (this.keySpecificTypedConsumer.get(key.getKeyCode()) != null) {
        this.keySpecificTypedConsumer.get(key.getKeyCode()).forEach(consumer -> consumer.accept(key));

        this.keyTypedConsumer.forEach(consumer -> consumer.accept(key));
        this.keyListeners.forEach(listener -> listener.keyTyped(key));
      }
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

  private static String getNormalText(KeyEvent e) {
    if (e.getExtendedKeyCode() == KeyEvent.getExtendedKeyCodeForChar('ß')) {
      return "ß";
    }

    switch (e.getKeyCode()) {
    case KeyEvent.VK_NUMPAD0:
      return "0";
    case KeyEvent.VK_NUMPAD1:
      return "1";
    case KeyEvent.VK_NUMPAD2:
      return "2";
    case KeyEvent.VK_NUMPAD3:
      return "3";
    case KeyEvent.VK_NUMPAD4:
      return "4";
    case KeyEvent.VK_NUMPAD5:
      return "5";
    case KeyEvent.VK_NUMPAD6:
      return "6";
    case KeyEvent.VK_NUMPAD7:
      return "7";
    case KeyEvent.VK_NUMPAD8:
      return "8";
    case KeyEvent.VK_NUMPAD9:
      return "9";

    case KeyEvent.VK_NUMBER_SIGN:
      return "#";
    case KeyEvent.VK_PERIOD:
      return ".";
    case KeyEvent.VK_COMMA:
      return ",";
    case KeyEvent.VK_PLUS:
    case KeyEvent.VK_ADD:
      return "+";
    case KeyEvent.VK_MINUS:
    case KeyEvent.VK_SUBTRACT:
      return "-";
    case KeyEvent.VK_MULTIPLY:
      return "*";
    case KeyEvent.VK_DIVIDE:
      return "/";
    default:
      if (KeyEvent.getKeyText(e.getKeyCode()).length() == 1) {
        String text = Character.toString(e.getKeyChar());
        text = text.toLowerCase();
        return text;
      } else {
        return "";
      }
    }
  }

  private static String getAltText(KeyEvent e) {
    if (e.getExtendedKeyCode() == KeyEvent.getExtendedKeyCodeForChar('ß')) {
      return "\\";
    }
    switch (e.getKeyCode()) {
    case KeyEvent.VK_0:
      return "}";
    case KeyEvent.VK_2:
      return "²";
    case KeyEvent.VK_3:
      return "³";
    case KeyEvent.VK_7:
      return "{";
    case KeyEvent.VK_8:
      return "[";
    case KeyEvent.VK_9:
      return "]";
    case KeyEvent.VK_E:
      return "€";
    case KeyEvent.VK_Q:
      return "@";
    case KeyEvent.VK_M:
      return "µ";
    case KeyEvent.VK_PLUS:
      return "~";
    default:
      return "";
    }
  }

  private static String getShiftText(KeyEvent e) {
    if (e.getExtendedKeyCode() == KeyEvent.getExtendedKeyCodeForChar('ß')) {
      return "?";
    }

    switch (e.getKeyCode()) {
    case KeyEvent.VK_0:
      return "=";
    case KeyEvent.VK_1:
      return "!";
    case KeyEvent.VK_2:
      return "\"";
    case KeyEvent.VK_3:
      return "§";
    case KeyEvent.VK_4:
      return "$";
    case KeyEvent.VK_5:
      return "%";
    case KeyEvent.VK_6:
      return "&";
    case KeyEvent.VK_7:
      return "/";
    case KeyEvent.VK_8:
      return "(";
    case KeyEvent.VK_9:
      return ")";
    case KeyEvent.VK_NUMBER_SIGN:
      return "'";
    case KeyEvent.VK_PERIOD:
      return ":";
    case KeyEvent.VK_COMMA:
      return ";";
    case KeyEvent.VK_PLUS:
      return "*";
    case KeyEvent.VK_MINUS:
      return "_";
    default:
      if (KeyEvent.getKeyText(e.getKeyCode()).length() == 1) {
        return Character.toString(e.getKeyChar());
      }

      return "";
    }
  }
}