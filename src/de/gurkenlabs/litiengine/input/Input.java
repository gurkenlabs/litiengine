package de.gurkenlabs.litiengine.input;

import java.awt.AWTException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameAdapter;

/**
 * The static <code>Input</code> class is the LITIengine's access point to devices that capture physical player input.
 * It manages input from different devices, i.e. keyboard, mouse or game pad, and provides a unified API to access this information.
 * 
 * @see #mouse()
 * @see #keyboard()
 * @see #gamepadManager()
 */
public final class Input {
  private static final Logger log = Logger.getLogger(Input.class.getName());


  private static GamepadManager gamePadManager;
  private static List<Gamepad> gamePads;
  private static IKeyboard keyboard;
  private static IMouse mouse;

  static {
  }

  private Input() {
    throw new UnsupportedOperationException();
  }

  public static GamepadManager gamepadManager() {
    return gamePadManager;
  }

  public static IKeyboard keyboard() {
    return keyboard;
  }

  public static IMouse mouse() {
    return mouse;
  }

  public static List<Gamepad> gamepads() {
    return gamePads;
  }

  /**
   * Gets the first game pad that is currently available.
   *
   * @return The first available {@link Gamepad} instance
   */
  public static Gamepad getGamepad() {
    if (gamePads.isEmpty()) {
      return null;
    }

    return gamePads.get(0);
  }

  /**
   * Gets the game pad with the specified index if it is still plugged in. After
   * re-plugging a controller while the game is running, its index might change.
   *
   * @param index
   *          The index of the {@link Gamepad}.
   * @return The {@link Gamepad} with the specified index.
   */
  public static Gamepad getGamepad(final int index) {
    if (gamePads.isEmpty()) {
      return null;
    }

    for (final Gamepad gamepad : gamePads) {
      if (gamepad.getIndex() == index) {
        return gamepad;
      }
    }

    return null;
  }

  private static final void init() {
    try {
      Input.keyboard = new Keyboard();
      mouse = new Mouse();
      if (Game.config().input().isGamepadSupport()) {
        gamePads = new CopyOnWriteArrayList<>();
        gamePadManager = new GamepadManager();
      }
    } catch (AWTException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public static final class InputGameAdapter extends GameAdapter {
    @Override
    public void terminated() {
      if (gamePadManager != null) {
        gamePadManager.terminate();
      }
    }

    @Override
    public void initialized(String... args) {
      Input.init();
    }

    @Override
    public void started() {
      if (gamePadManager != null) {
        gamePadManager.start();
      }
    }
  }
}
