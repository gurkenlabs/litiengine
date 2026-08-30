package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
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

/// Convenient, managed input helper for scripts.
///
/// All listeners registered through this class are automatically cleaned up when the owning
/// [ScriptContext] is detached or reloaded.
public final class ScriptInput {
  private final ScriptContext<?> context;

  ScriptInput(ScriptContext<?> context) {
    this.context = Objects.requireNonNull(context, "ScriptContext must not be null.");
  }

  /// Checks whether the key with the specified `keyCode` is currently pressed.
  /// @param keyCode An AWT virtual key code.
  /// @return `true` while the key is pressed.
  public boolean isPressed(int keyCode) {
    return Input.keyboard() != null && Input.keyboard().isPressed(keyCode);
  }

  /// Checks whether the key with the specified name (e.g., "SPACE", "W", "ENTER", "ESCAPE") is currently pressed.
  /// @param keyName A case-insensitive [KeyEvent] `VK_` field name, with or without the prefix.
  /// @return `true` while the resolved key is pressed.
  public boolean isPressed(String keyName) {
    int keyCode = resolveKeyCode(keyName);
    return keyCode != KeyEvent.VK_UNDEFINED && isPressed(keyCode);
  }

  /// Checks whether the key with the specified `keyCode` was recently released.
  /// @param keyCode An AWT virtual key code.
  /// @return `true` if the key was recently released.
  public boolean wasReleased(int keyCode) {
    return Input.keyboard() != null && Input.keyboard().wasReleased(keyCode);
  }

  /// Binds an action to be executed when the specified key is pressed.
  /// @param keyCode An AWT virtual key code.
  /// @param onPress The action to invoke.
  /// @return A subscription that removes the listener.
  public Subscription bindKey(int keyCode, Runnable onPress) {
    Objects.requireNonNull(onPress, "onPress must not be null.");
    IKeyboard.KeyPressedListener listener = e -> {
      if (!Game.scripts().isEnabled()) return;
      if (e.getKeyCode() == keyCode) {
        onPress.run();
      }
    };
    IKeyboard keyboard = Input.keyboard();
    if (keyboard != null) {
      keyboard.onKeyPressed(keyCode, listener);
      return this.context.manage(() -> keyboard.removeKeyPressedListener(keyCode, listener));
    }
    return () -> {};
  }

  /// Binds actions to be executed when the specified key is pressed and released.
  /// @param keyCode An AWT virtual key code.
  /// @param onPress The press action, or `null` for none.
  /// @param onRelease The release action, or `null` for none.
  /// @return A subscription that removes both listeners.
  public Subscription bindKey(int keyCode, Runnable onPress, Runnable onRelease) {
    Subscription pressSub = onPress != null ? bindKey(keyCode, onPress) : () -> {};
    Subscription releaseSub = () -> {};
    IKeyboard keyboard = Input.keyboard();
    if (onRelease != null && keyboard != null) {
      IKeyboard.KeyReleasedListener listener = e -> {
        if (!Game.scripts().isEnabled()) return;
        if (e.getKeyCode() == keyCode) {
          onRelease.run();
        }
      };
      keyboard.onKeyReleased(keyCode, listener);
      releaseSub = this.context.manage(() -> keyboard.removeKeyReleasedListener(keyCode, listener));
    }
    Subscription finalReleaseSub = releaseSub;
    return () -> {
      pressSub.unsubscribe();
      finalReleaseSub.unsubscribe();
    };
  }

  /// Binds a consumer to receive key press events for the specified key code.
  /// @param keyCode An AWT virtual key code.
  /// @param onPress The event consumer.
  /// @return A subscription that removes the listener.
  public Subscription bindKey(int keyCode, Consumer<KeyEvent> onPress) {
    Objects.requireNonNull(onPress, "onPress must not be null.");
    IKeyboard.KeyPressedListener listener = e -> {
      if (!Game.scripts().isEnabled()) return;
      onPress.accept(e);
    };
    IKeyboard keyboard = Input.keyboard();
    if (keyboard != null) {
      keyboard.onKeyPressed(keyCode, listener);
      return this.context.manage(() -> keyboard.removeKeyPressedListener(keyCode, listener));
    }
    return () -> {};
  }

  /// Binds a consumer to receive key typed events for the specified key code.
  /// @param keyCode An AWT virtual key code.
  /// @param onTyped The event consumer.
  /// @return A subscription that removes the listener.
  public Subscription bindKeyTyped(int keyCode, Consumer<KeyEvent> onTyped) {
    Objects.requireNonNull(onTyped, "onTyped must not be null.");
    IKeyboard.KeyTypedListener listener = e -> {
      if (!Game.scripts().isEnabled()) return;
      onTyped.accept(e);
    };
    IKeyboard keyboard = Input.keyboard();
    if (keyboard != null) {
      keyboard.onKeyTyped(keyCode, listener);
      return this.context.manage(() -> keyboard.removeKeyTypedListener(keyCode, listener));
    }
    return () -> {};
  }

  /// Checks whether any mouse button is currently pressed.
  /// @return `true` while a mouse button is pressed.
  public boolean isMouseButtonPressed() {
    return Input.mouse() != null && Input.mouse().isPressed();
  }

  /// Checks whether the left mouse button is currently pressed.
  /// @return `true` while the left button is pressed.
  public boolean isLeftMouseButtonPressed() {
    return Input.mouse() != null && Input.mouse().isLeftButtonPressed();
  }

  /// Checks whether the right mouse button is currently pressed.
  /// @return `true` while the right button is pressed.
  public boolean isRightMouseButtonPressed() {
    return Input.mouse() != null && Input.mouse().isRightButtonPressed();
  }

  /// Gets the current mouse screen location relative to the game window.
  /// @return The screen location, or `(0,0)` when no mouse is available.
  public Point2D mouseLocation() {
    return Input.mouse() != null ? Input.mouse().getLocation() : new Point2D.Double(0, 0);
  }

  /// Gets the current mouse world/map location translated via camera.
  /// @return The map location, or `(0,0)` when no mouse is available.
  public Point2D mouseWorldLocation() {
    return Input.mouse() != null ? Input.mouse().getMapLocation() : new Point2D.Double(0, 0);
  }

  /// Gets the map tile coordinate currently under the mouse.
  /// @return The tile coordinate, or `(0,0)` when no mouse is available.
  public Point mouseTile() {
    return Input.mouse() != null ? Input.mouse().getTile() : new Point(0, 0);
  }

  /// Binds an action to be executed when the specified mouse button is pressed (e.g. [MouseEvent#BUTTON1]).
  /// @param button An AWT mouse button identifier.
  /// @param onPress The action to invoke.
  /// @return A subscription that removes the listener.
  public Subscription bindMouse(int button, Runnable onPress) {
    Objects.requireNonNull(onPress, "onPress must not be null.");
    IMouse.MousePressedListener listener = e -> {
      if (!Game.scripts().isEnabled()) return;
      if (e.getButton() == button) {
        onPress.run();
      }
    };
    IMouse mouse = Input.mouse();
    if (mouse != null) {
      mouse.onPressed(listener);
      return this.context.manage(() -> mouse.removeMousePressedListener(listener));
    }
    return () -> {};
  }

  /// Binds actions for when the specified mouse button is pressed and released.
  /// @param button An AWT mouse button identifier.
  /// @param onPress The press action, or `null` for none.
  /// @param onRelease The release action, or `null` for none.
  /// @return A subscription that removes both listeners.
  public Subscription bindMouse(int button, Runnable onPress, Runnable onRelease) {
    Subscription pressSub = onPress != null ? bindMouse(button, onPress) : () -> {};
    Subscription releaseSub = () -> {};
    IMouse mouse = Input.mouse();
    if (onRelease != null && mouse != null) {
      IMouse.MouseReleasedListener listener = e -> {
        if (!Game.scripts().isEnabled()) return;
        if (e.getButton() == button) {
          onRelease.run();
        }
      };
      mouse.onReleased(listener);
      releaseSub = this.context.manage(() -> mouse.removeMouseReleasedListener(listener));
    }
    Subscription finalReleaseSub = releaseSub;
    return () -> {
      pressSub.unsubscribe();
      finalReleaseSub.unsubscribe();
    };
  }

  /// Resolves a key name like "SPACE", "ESCAPE", "W" to a [KeyEvent] VK_ constant code.
  /// @param name The case-insensitive key name.
  /// @return The virtual key code, or [KeyEvent#VK_UNDEFINED] if it cannot be resolved.
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
