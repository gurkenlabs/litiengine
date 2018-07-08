package de.gurkenlabs.litiengine.input;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.IUpdateable;

/**
 * The listener interface for receiving userKey events. The class that is
 * interested in processing a userKey event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addUserKeyListener</code> method. When the userKey event
 * occurs, that object's appropriate method is invoked.
 */
public class Keyboard implements KeyEventDispatcher, IUpdateable {

  private boolean consumeAlt;
  /** The key observers. */
  private final List<IKeyObserver> keyObservers;
  private final List<Map.Entry<Integer, Consumer<KeyEvent>>> keySpecificPressedConsumer;
  private final List<Map.Entry<Integer, Consumer<KeyEvent>>> keySpecificReleasedConsumer;
  private final List<Map.Entry<Integer, Consumer<KeyEvent>>> keySpecificTypedConsumer;
  private final List<Consumer<KeyEvent>> keyPressedConsumer;
  private final List<Consumer<KeyEvent>> keyReleasedConsumer;
  private final List<Consumer<KeyEvent>> keyTypedConsumer;

  /** The pressed keys. */
  private final List<KeyEvent> pressedKeys;

  /** The released keys. */
  private final List<KeyEvent> releasedKeys;

  /** The typed keys. */
  private final List<KeyEvent> typedKeys;

  /**
   * Instantiates a new key board.
   */
  public Keyboard() {
    this.keySpecificTypedConsumer = new CopyOnWriteArrayList<>();
    this.keySpecificPressedConsumer = new CopyOnWriteArrayList<>();
    this.keySpecificReleasedConsumer = new CopyOnWriteArrayList<>();
    this.keyPressedConsumer = new CopyOnWriteArrayList<>();
    this.keyReleasedConsumer = new CopyOnWriteArrayList<>();
    this.keyTypedConsumer = new CopyOnWriteArrayList<>();

    this.pressedKeys = new CopyOnWriteArrayList<>();
    this.releasedKeys = new CopyOnWriteArrayList<>();
    this.typedKeys = new CopyOnWriteArrayList<>();
    this.keyObservers = new CopyOnWriteArrayList<>();

    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
    Input.getLoop().attach(this);
  }

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

  public String getText(final KeyEvent e) {
    if (this.isPressed(KeyEvent.VK_SHIFT) || Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK)) {
      return this.getShiftText(e);
    } else if (this.isPressed(KeyEvent.VK_ALT_GRAPH)) {
      return this.getAltText(e);
    } else {
      return this.getNormalText(e);
    }
  }

  public boolean isPressed(final int keyCode) {
    for (final KeyEvent key : this.pressedKeys) {
      if (key.getKeyCode() == keyCode) {
        return true;
      }
    }

    return false;
  }

  public void onKeyPressed(final int keyCode, final Consumer<KeyEvent> consumer) {
    this.keySpecificPressedConsumer.add(new AbstractMap.SimpleEntry<>(keyCode, consumer));
  }

  public void onKeyReleased(final int keyCode, final Consumer<KeyEvent> consumer) {
    this.keySpecificReleasedConsumer.add(new AbstractMap.SimpleEntry<>(keyCode, consumer));
  }

  public void onKeyTyped(final int keyCode, final Consumer<KeyEvent> consumer) {
    this.keySpecificTypedConsumer.add(new AbstractMap.SimpleEntry<>(keyCode, consumer));
  }

  public void onKeyPressed(Consumer<KeyEvent> consumer) {
    this.keyPressedConsumer.add(consumer);
  }

  public void onKeyReleased(Consumer<KeyEvent> consumer) {
    this.keyReleasedConsumer.add(consumer);
  }

  public void onKeyTyped(Consumer<KeyEvent> consumer) {
    this.keyTypedConsumer.add(consumer);
  }

  /**
   * Register for key events.
   *
   * @param observer
   *          the observer
   */
  public void addKeyObserver(final IKeyObserver observer) {
    if (this.keyObservers.contains(observer)) {
      return;
    }

    this.keyObservers.add(observer);
  }

  /**
   * Unregister from key down events.
   *
   * @param observer
   *          the observer
   */
  public void removeKeyObserver(final IKeyObserver observer) {
    this.keyObservers.remove(observer);
  }

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
      this.keySpecificPressedConsumer.forEach(consumer -> {
        if (consumer.getKey().intValue() == key.getKeyCode()) {
          consumer.getValue().accept(key);
        }
      });

      this.keyPressedConsumer.forEach(consumer -> consumer.accept(key));
      this.keyObservers.forEach(observer -> observer.handlePressedKey(key));
    });
  }

  /**
   * Execute released keys.
   */
  private void executeReleasedKeys() {
    this.releasedKeys.forEach(key -> {
      this.keySpecificReleasedConsumer.forEach(consumer -> {
        if (consumer.getKey().intValue() == key.getKeyCode()) {
          consumer.getValue().accept(key);
        }
      });

      this.keyReleasedConsumer.forEach(consumer -> consumer.accept(key));
      this.keyObservers.forEach(observer -> observer.handleReleasedKey(key));
    });

    this.releasedKeys.clear();
  }

  /**
   * Execute typed keys.
   */
  private void executeTypedKeys() {
    this.typedKeys.forEach(key -> {
      this.keySpecificTypedConsumer.forEach(consumer -> {
        if (consumer.getKey().intValue() == key.getKeyCode()) {
          consumer.getValue().accept(key);
        }
      });

      this.keyTypedConsumer.forEach(consumer -> consumer.accept(key));
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
  private void removePressedKey(final KeyEvent keyCode) {
    for (final KeyEvent removeKey : this.pressedKeys) {
      if (removeKey.getKeyCode() == keyCode.getKeyCode()) {
        this.pressedKeys.remove(removeKey);
        return;
      }
    }
  }

  private String getNormalText(KeyEvent e) {
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

  private String getAltText(KeyEvent e) {
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

  private String getShiftText(KeyEvent e) {
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