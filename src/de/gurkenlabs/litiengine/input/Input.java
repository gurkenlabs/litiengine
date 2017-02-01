/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.input;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Controller.Type;

// TODO: Auto-generated Javadoc
/**
 * The Class Input.
 */
public class Input {

  /** The keyboard. */
  public static IKeyboard KEYBOARD = new KeyBoard();

  /** The mouse. */
  public static IMouse MOUSE = new Mouse();

  public static IGamepadManager GAMEPADMANAGER = new GamepadManager();

  public static final List<IGamepad> GAMEPADS = new CopyOnWriteArrayList<>();

  public static IGamepad getGamepad() {
    if (GAMEPADS.size() == 0) {
      return null;
    }

    return GAMEPADS.get(0);
  }

  public static IGamepad getGamepad(int index) {
    if (GAMEPADS.size() == 0) {
      return null;
    }

    for (IGamepad gamepad : GAMEPADS) {
      if (gamepad.getIndex() == index) {
        return gamepad;
      }
    }

    return null;
  }
}
