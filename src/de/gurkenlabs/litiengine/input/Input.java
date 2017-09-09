package de.gurkenlabs.litiengine.input;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameLoop;

/**
 * The Class Input.
 */
public final class Input {

  private static IGamepadManager gamePadManager;

  private static List<IGamepad> gamePads;

  /** The keyboard. */
  private static IKeyboard keyboard;

  /** The mouse. */
  private static IMouse mouse;

  // we need an own gameloop because otherwise input won't work if the game has
  // been paused
  protected static final GameLoop InputLoop = new GameLoop(Game.getLoop().getUpdateRate());

  private Input() {
  }

  public static void init() {
    keyboard = new KeyBoard();
    mouse = new Mouse();
    if (Game.getConfiguration().INPUT.isGamepadSupport()) {
      gamePads = new CopyOnWriteArrayList<>();
      gamePadManager = new GamepadManager();
    }

    InputLoop.start();
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
   * Gets the first gamepad that is currently available.
   *
   * @return
   */
  public static IGamepad getGamepad() {
    if (gamePads.isEmpty()) {
      return null;
    }

    return gamePads.get(0);
  }

  /**
   * Gets the gamepad with the specified index if it is still plugged in. After
   * replugging a controller while the game is running, its index might change.
   *
   * @param index
   * @return
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
}
