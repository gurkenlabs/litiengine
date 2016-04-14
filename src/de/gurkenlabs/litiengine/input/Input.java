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
  public static IKeyboard KEYBOARD = new KeyBoard();;

  /** The mouse. */
  public static IMouse MOUSE;

  public static void init() {
    MOUSE = new Mouse();

    Game.getLoop().registerForUpdate(KEYBOARD);
  }
}
