package de.gurkenlabs.litiengine.input;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameLoop;

/**
 * The Class Input.
 */
public class Input {

  public static IGamepadManager GAMEPADMANAGER;

  public static List<IGamepad> GAMEPADS;

  /** The keyboard. */
  public static IKeyboard KEYBOARD;

  /** The mouse. */
  public static IMouse MOUSE;

  // we need an own gameloop because otherwise input won't work if the game has
  // been paused
  protected static GameLoop InputLoop;

  private Input() {
  }

  public static void init() {
    InputLoop = new GameLoop(Game.getLoop().getUpdateRate());

    KEYBOARD = new KeyBoard();
    MOUSE = new Mouse();
    if (Game.getConfiguration().INPUT.isGamepadSupport()) {
      GAMEPADS = new CopyOnWriteArrayList<>();
      GAMEPADMANAGER = new GamepadManager();
    }

    InputLoop.start();
  }

  /**
   * Gets the first gamepad that is currently available.
   *
   * @return
   */
  public static IGamepad getGamepad() {
    if (GAMEPADS.isEmpty()) {
      return null;
    }

    return GAMEPADS.get(0);
  }

  /**
   * Gets the gamepad with the specified index if it is still plugged in. After
   * replugging a controller while the game is running, its index might change.
   *
   * @param index
   * @return
   */
  public static IGamepad getGamepad(final int index) {
    if (GAMEPADS.isEmpty()) {
      return null;
    }

    for (final IGamepad gamepad : GAMEPADS) {
      if (gamepad.getIndex() == index) {
        return gamepad;
      }
    }

    return null;
  }
}
