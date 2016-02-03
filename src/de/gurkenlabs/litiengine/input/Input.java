/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.Game;

// TODO: Auto-generated Javadoc
/**
 * The Class Input.
 */
public class Input {

  /** The keyboard. */
  public static IKeyboard KEYBOARD;

  /** The mouse. */
  public static IMouse MOUSE;

  public static void init() {
    KEYBOARD = new KeyBoard();
    MOUSE = new Mouse();

    Game.getLoop().registerForUpdate(KEYBOARD);
    Game.getLoop().registerForUpdate(MOUSE);
  }
}
