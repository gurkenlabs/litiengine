package de.gurkenlabs.litiengine.input;

import java.awt.AWTException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;

/**
 * The static {@code Input} class is the LITIENGINE's access point to devices that capture physical player input.
 * It manages input from different devices, i.e. keyboard, mouse or gamepad, and provides a unified API to access this information.
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

  /**
   * Gets the manager for all gamepad input devices.
   * 
   * <p>
   * The manager provides easy access to the default controller as well as access by gamepad index for mulitplayer games.
   * Gamepads don't need to be added explicitly, the manager supports hot-plugging at runtime and will auto-detect any
   * added/removed gamepads.
   * </p>
   * 
   * <p>
   * <b>This returns null if {@code Game.config().input().isGamepadSupport()} is set to false.</b>
   * </p>
   * 
   * @return The gamepad manager.
   * 
   * @see GamepadManager#current()
   * @see GamepadManager#get(int)
   */
  public static GamepadManager gamepads() {
    if (!Game.config().input().isGamepadSupport()) {
      log.log(Level.SEVERE, "Cannot access gamepads because gamepad support is disabled in the configuration.");
    }

    return gamePadManager;
  }

  /**
   * Gets the keyboard input device.
   * 
   * @return The keyboard input device.
   */
  public static IKeyboard keyboard() {
    return keyboard;
  }

  /**
   * Gets the mouse input device.
   * 
   * @return The mouse input device.
   */
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
        Mouse m = new Mouse();

        if (!Game.isInNoGUIMode()) {
          Game.window().getRenderComponent().addMouseListener(m);
          Game.window().getRenderComponent().addMouseMotionListener(m);
          Game.window().getRenderComponent().addMouseWheelListener(m);
        }

        mouse = m;
        if (Game.config().input().isGamepadSupport()) {
          gamePadManager = new GamepadManager();
        }
      } catch (AWTException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }
}
