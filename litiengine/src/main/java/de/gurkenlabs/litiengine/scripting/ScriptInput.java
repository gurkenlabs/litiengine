package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.input.IKeyboard;
import de.gurkenlabs.litiengine.input.IMouse;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Convenient, managed input helper for scripts.
 *
 * <p>All listeners registered through this class are automatically cleaned up when the owning
 * {@link ScriptContext} is detached or reloaded.
 */
public final class ScriptInput {
  private final ScriptContext<?> context;

  ScriptInput(ScriptContext<?> context) {
    this.context = Objects.requireNonNull(context, "ScriptContext must not be null.");
  }

  /** Checks whether the key with the specified {@code keyCode} is currently pressed. */
  public boolean isPressed(int keyCode) {
    return Input.keyboard() != null && Input.keyboard().isPressed(keyCode);
  }

  /**
   * Checks whether the key with the specified name (e.g., "SPACE", "W", "ENTER", "ESCAPE") is currently pressed.
   */
  public boolean isPressed(String keyName) {
    int keyCode = resolveKeyCode(keyName);
    return keyCode != KeyEvent.VK_UNDEFINED && isPressed(keyCode);
  }

  /** Checks whether the key with the specified {@code keyCode} was recently released. */
  public boolean wasReleased(int keyCode) {
    return Input.keyboard() != null && Input.keyboard().wasReleased(keyCode);
  }

  /** Binds an action to be executed when the specified key is pressed. */
  public Subscription bindKey(int keyCode, Runnable onPress) {
    Objects.requireNonNull(onPress, "onPress must not be null.");
    IKeyboard.KeyPressedListener listener = e -> {
      if (e.getKeyCode() == keyCode) {
        onPress.run();
      }
    };
    if (Input.keyboard() != null) {
      Input.keyboard().onKeyPressed(keyCode, listener);
      return this.context.manage(() -> Input.keyboard().removeKeyPressedListener(keyCode, listener));
    }
    return () -> {};
  }

  /** Binds actions to be executed when the specified key is pressed and released. */
  public Subscription bindKey(int keyCode, Runnable onPress, Runnable onRelease) {
    Subscription pressSub = onPress != null ? bindKey(keyCode, onPress) : () -> {};
    Subscription releaseSub = () -> {};
    if (onRelease != null && Input.keyboard() != null) {
      IKeyboard.KeyReleasedListener listener = e -> {
        if (e.getKeyCode() == keyCode) {
          onRelease.run();
        }
      };
      Input.keyboard().onKeyReleased(keyCode, listener);
      releaseSub = this.context.manage(() -> Input.keyboard().removeKeyReleasedListener(keyCode, listener));
    }
    Subscription finalReleaseSub = releaseSub;
    return () -> {
      pressSub.unsubscribe();
      finalReleaseSub.unsubscribe();
    };
  }

  /** Binds a consumer to receive key press events for the specified key code. */
  public Subscription bindKey(int keyCode, Consumer<KeyEvent> onPress) {
    Objects.requireNonNull(onPress, "onPress must not be null.");
    IKeyboard.KeyPressedListener listener = onPress::accept;
    if (Input.keyboard() != null) {
      Input.keyboard().onKeyPressed(keyCode, listener);
      return this.context.manage(() -> Input.keyboard().removeKeyPressedListener(keyCode, listener));
    }
    return () -> {};
  }

  /** Checks whether any mouse button is currently pressed. */
  public boolean isMouseButtonPressed() {
    return Input.mouse() != null && Input.mouse().isPressed();
  }

  /** Checks whether the left mouse button is currently pressed. */
  public boolean isLeftMouseButtonPressed() {
    return Input.mouse() != null && Input.mouse().isLeftButtonPressed();
  }

  /** Checks whether the right mouse button is currently pressed. */
  public boolean isRightMouseButtonPressed() {
    return Input.mouse() != null && Input.mouse().isRightButtonPressed();
  }

  /** Gets the current mouse screen location relative to the game window. */
  public Point2D mouseLocation() {
    return Input.mouse() != null ? Input.mouse().getLocation() : new Point2D.Double(0, 0);
  }

  /** Gets the current mouse world/map location translated via camera. */
  public Point2D mouseWorldLocation() {
    return Input.mouse() != null ? Input.mouse().getMapLocation() : new Point2D.Double(0, 0);
  }

  /** Gets the map tile coordinate currently under the mouse. */
  public Point mouseTile() {
    return Input.mouse() != null ? Input.mouse().getTile() : new Point(0, 0);
  }

  /** Binds an action to be executed when the specified mouse button is pressed (e.g. {@link MouseEvent#BUTTON1}). */
  public Subscription bindMouse(int button, Runnable onPress) {
    Objects.requireNonNull(onPress, "onPress must not be null.");
    IMouse.MousePressedListener listener = e -> {
      if (e.getButton() == button) {
        onPress.run();
      }
    };
    if (Input.mouse() != null) {
      Input.mouse().onPressed(listener);
      return this.context.manage(() -> Input.mouse().removeMousePressedListener(listener));
    }
    return () -> {};
  }

  /** Binds actions for when the specified mouse button is pressed and released. */
  public Subscription bindMouse(int button, Runnable onPress, Runnable onRelease) {
    Subscription pressSub = onPress != null ? bindMouse(button, onPress) : () -> {};
    Subscription releaseSub = () -> {};
    if (onRelease != null && Input.mouse() != null) {
      IMouse.MouseReleasedListener listener = e -> {
        if (e.getButton() == button) {
          onRelease.run();
        }
      };
      Input.mouse().onReleased(listener);
      releaseSub = this.context.manage(() -> Input.mouse().removeMouseReleasedListener(listener));
    }
    Subscription finalReleaseSub = releaseSub;
    return () -> {
      pressSub.unsubscribe();
      finalReleaseSub.unsubscribe();
    };
  }

  /** Resolves a key name like "SPACE", "ESCAPE", "W" to a {@link KeyEvent} VK_ constant code. */
  public static int resolveKeyCode(String name) {
    if (name == null || name.isBlank()) return KeyEvent.VK_UNDEFINED;
    String formatted = name.trim().toUpperCase();
    if (!formatted.startsWith("VK_")) {
      formatted = "VK_" + formatted;
    }
    try {
      Field field = KeyEvent.class.getField(formatted);
      return field.getInt(null);
    } catch (Exception ignored) {
      if (name.length() == 1) {
        char c = Character.toUpperCase(name.charAt(0));
        return KeyEvent.getExtendedKeyCodeForChar(c);
      }
      return KeyEvent.VK_UNDEFINED;
    }
  }
}
