package de.gurkenlabs.litiengine.input;

import java.awt.AWTException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameLoop;

public final class Input {
  // we need an own update loop because otherwise input won't work if the game has
  // been paused
  private static final GameLoop InputLoop;
  private static GamepadManager gamePadManager;
  private static List<Gamepad> gamePads;
  private static Keyboard keyboard;
  private static Mouse mouse;

  static {
    InputLoop = new GameLoop(Game.getLoop().getUpdateRate());
  }

  private Input() {
  }

  public static void init() throws AWTException {
    keyboard = new Keyboard();
    mouse = new Mouse();
    if (Game.getConfiguration().input().isGamepadSupport()) {
      gamePads = new CopyOnWriteArrayList<>();
      gamePadManager = new GamepadManager();
    }
  }

  public static void start() {
    InputLoop.start();
    if (gamePadManager != null) {
      gamePadManager.start();
    }
  }

  public static void terminate() {
    InputLoop.terminate();
    if (gamePadManager != null) {
      gamePadManager.terminate();
    }
  }

  public static GameLoop getLoop() {
    return InputLoop;
  }

  public static GamepadManager gamepadManager() {
    return gamePadManager;
  }

  public static Keyboard keyboard() {
    return keyboard;
  }

  public static Mouse mouse() {
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
}
