package de.gurkenlabs.litiengine.input;

import java.awt.AWTException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;

/**
 * The static <code>Input</code> class is the LITIengine's access point to devices that capture physical player input.
 * It manages input from different devices, i.e. keyboard, mouse or game pad, and provides a unified API to access this information.
 * 
 * @see #mouse()
 * @see #keyboard()
 * @see #gamepads()
 */
public final class Input {
  private static final Logger log = Logger.getLogger(Input.class.getName());

  private static GamepadManager gamePadManager;
  private static IKeyboard keyboard;
  private static IMouse mouse;

  private Input() {
    throw new UnsupportedOperationException();
  }

  public static GamepadManager gamepads() {
    if (!Game.config().input().isGamepadSupport()) {
      log.log(Level.SEVERE, "Cannot access gamepads because gamepad support is disabled in the configuration.");
    }
    
    return gamePadManager;
  }

  public static IKeyboard keyboard() {
    return keyboard;
  }

  public static IMouse mouse() {
    return mouse;
  }

  public static final class InputGameAdapter implements GameListener {
    @Override
    public void terminated() {
      if (gamePadManager != null) {
        gamePadManager.terminate();
      }
    }

    @Override
    public void initialized(String... args) {
      init();
    }

    @Override
    public void started() {
      if (gamePadManager != null) {
        gamePadManager.start();
      }
    }

    private static void init() {
      try {
        keyboard = new Keyboard();
        mouse = new Mouse();
        if (Game.config().input().isGamepadSupport()) {
          gamePadManager = new GamepadManager();
        }
      } catch (AWTException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }
}
