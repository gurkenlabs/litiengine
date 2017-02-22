/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.input;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.GameLoop;

/**
 * The Class Input.
 */
public class Input {

  public static IGamepadManager GAMEPADMANAGER;

  public static final List<IGamepad> GAMEPADS;

  /** The keyboard. */
  public static IKeyboard KEYBOARD;

  /** The mouse. */
  public static IMouse MOUSE;

  // we need an own gameloop because otherwise input won't work if the game has
  // been paused
  protected static final GameLoop GameadLoop;

  static {
    GAMEPADS = new CopyOnWriteArrayList<>();
    GameadLoop = new GameLoop(30);
    GameadLoop.start();
    KEYBOARD = new KeyBoard();
    MOUSE = new Mouse();
    GAMEPADMANAGER = new GamepadManager();
  }

  /**
   * Gets the first gamepad that is currently available.
   *
   * @return
   */
  public static IGamepad getGamepad() {
    if (GAMEPADS.size() == 0) {
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
    if (GAMEPADS.size() == 0) {
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
