package de.gurkenlabs.litiengine.input;

import java.awt.AWTException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameAdapter;
import de.gurkenlabs.litiengine.GameLoop;
import de.gurkenlabs.litiengine.IGameLoop;

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

  private static final IGameLoop InputLoop;
  private static IGamepadManager gamePadManager;
  private static List<IGamepad> gamePads;
  private static IKeyboard keyboard;
  private static IMouse mouse;

  static {
    // we need an own update loop because otherwise input won't work if the game has been paused
    InputLoop = new GameLoop("Input Loop", Game.getLoop().getUpdateRate());
  }

  private Input() {
  }

  public static IGameLoop getLoop() {
    return InputLoop;
  }

  public static IGamepadManager gamepadManager() {
    return gamePadManager;
  }

  public static IKeyboard keyboard() {
    return keyboard;
  }

  public static IMouse mouse() {
    return mouse;
  }

  public static List<IGamepad> gamepads() {
    return gamePads;
  }

  /**
   * Gets the first game pad that is currently available.
   *
   * @return The first available {@link IGamepad} instance
   */
  public static IGamepad getGamepad() {
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
   *          The index of the {@link IGamepad}.
   * @return The {@link IGamepad} with the specified index.
   */
  public static IGamepad getGamepad(final int index) {
    if (gamePads.isEmpty()) {
      return null;
    }

    for (final IGamepad gamepad : gamePads) {
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
      if (Game.getConfiguration().input().isGamepadSupport()) {
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
      InputLoop.terminate();
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
      InputLoop.start();
      if (gamePadManager != null) {
        gamePadManager.start();
      }
    }
  }
}
