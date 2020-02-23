package de.gurkenlabs.litiengine.input;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.ILaunchable;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.java.games.input.ControllerEnvironment;

public final class GamepadManager extends GamepadEvents implements ILaunchable {
  private static final Logger log = Logger.getLogger(GamepadManager.class.getName());

  private final Collection<GamepadAddedListener> gamepadAddedConsumer;
  private final Collection<GamepadRemovedListener> gamepadRemovedConsumer;

  private final List<Gamepad> gamePads;

  private final Thread hotPlugThread;

  private int defaultgamePadIndex = -1;
  private boolean handleHotPluggedControllers;

  GamepadManager() {
    this.gamepadRemovedConsumer = ConcurrentHashMap.newKeySet();
    this.gamepadAddedConsumer = ConcurrentHashMap.newKeySet();

    this.gamePads = new CopyOnWriteArrayList<>();

    this.hotPlugThread = new Thread(() -> {
      while (!Thread.interrupted()) {
        this.updateGamepads();

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    });

    Game.addGameListener(new GameListener() {
      @Override
      public void terminated() {
        hotPlugThread.interrupt();
      }
    });

    this.onAdded(pad -> {
      if (this.defaultgamePadIndex == -1) {
        this.defaultgamePadIndex = pad.getIndex();
        this.hookupToGamepad(pad);
      }
    });

    this.onRemoved(pad -> {
      if (this.defaultgamePadIndex == pad.getIndex()) {
        this.defaultgamePadIndex = -1;
        final Gamepad newGamePad = current();
        if (newGamePad != null) {
          this.defaultgamePadIndex = newGamePad.getIndex();
          this.hookupToGamepad(newGamePad);
        }
      }
    });

    updateGamepads();
  }

  /**
   * Adds the specified gamepad added listener to receive events when gamepads are added.
   * 
   * @param listener
   *          The listener to add.
   */
  public void onAdded(final GamepadAddedListener listener) {
    this.gamepadAddedConsumer.add(listener);
  }

  /**
   * Unregister the specified added listener from this instance.
   *
   * @param listener
   *          The listener to remove.
   */
  public void removeAddedListener(GamepadAddedListener listener) {
    this.gamepadAddedConsumer.remove(listener);
  }

  /**
   * Adds the specified gamepad removed listener to receive events when gamepads are removed.
   * 
   * @param listener
   *          The listener to add.
   */
  public void onRemoved(final GamepadRemovedListener listener) {
    this.gamepadRemovedConsumer.add(listener);
  }

  /**
   * Unregister the specified removed listener from this instance.
   *
   * @param listener
   *          The listener to remove.
   */
  public void removeRemovedListener(GamepadRemovedListener listener) {
    this.gamepadRemovedConsumer.remove(listener);
  }

  /**
   * Gets all gamepads that are currently available.
   * 
   * @return All available gamepads.
   * 
   * @see #get(int)
   * @see #current()
   */
  public List<Gamepad> getAll() {
    return this.gamePads;
  }

  /**
   * Gets the first gamepad that is currently available.
   *
   * @return The first available {@link Gamepad} instance
   * 
   * @see #get(int)
   * @see #getAll()
   */
  public Gamepad current() {
    if (this.gamePads.isEmpty()) {
      return null;
    }

    return this.gamePads.get(0);
  }

  /**
   * Gets the gamepad with the specified index if it is still plugged in. After
   * re-plugging a controller while the game is running, its index might change.
   *
   * @param index
   *          The index of the {@link Gamepad}.
   * @return The {@link Gamepad} with the specified index.
   * 
   * @see #getAll()
   * @see #current()
   */
  public Gamepad get(final int index) {
    for (final Gamepad gamepad : this.gamePads) {
      if (gamepad.getIndex() == index) {
        return gamepad;
      }
    }

    return null;
  }

  @Override
  public boolean isPressed(String gamepadComponent) {
    final Gamepad current = this.current();
    return current != null && current.isPressed(gamepadComponent);
  }

  /**
   * DON'T CALL THIS EXPLICITLY! THE LITIENGINE WILL MANAGE THE LIFECYCLE OF THIS INSTANCE.
   */
  @Override
  public void start() {
    this.hotPlugThread.start();
  }

  /**
   * DON'T CALL THIS EXPLICITLY! THE LITIENGINE WILL MANAGE THE LIFECYCLE OF THIS INSTANCE.
   */
  @Override
  public void terminate() {
    int totalWait = 0;
    while (handleHotPluggedControllers && totalWait <= 40) {
      try {
        Thread.sleep(50);
      } catch (Exception e) {
        break;
      }
      totalWait++;
    }

    this.hotPlugThread.interrupt();
  }

  /**
   * DON'T CALL THIS EXPLICITLY! THE LITIENGINE WILL MANAGE THE LIFECYCLE OF GAMEPADS.
   */
  void remove(final Gamepad gamepad) {
    if (gamepad == null) {
      return;
    }

    this.getAll().remove(gamepad);
    for (final GamepadRemovedListener listener : this.gamepadRemovedConsumer) {
      listener.removed(gamepad);
    }
  }

  /**
   * In JInput it is not possible to get newly added controllers or detached
   * controllers because it will never update its controllers. If you would
   * restart the application it would work... so we just reset the environment via
   * reflection and it'll do it ;).
   */
  private static void hackTheShitOutOfJInput() {
    try {
      final Field env = ControllerEnvironment.class.getDeclaredField("defaultEnvironment");
      env.setAccessible(true);
      final Class<?> clazz = Class.forName("net.java.games.input.DefaultControllerEnvironment");

      // kill threads that might still be running.
      // otherwise we would spawn a new thread every time this method is called
      // without killing the last one
      final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
      for (final Thread thread : threadSet) {
        final String name = thread.getClass().getName();
        if (name.equals("net.java.games.input.RawInputEventQueue$QueueThread")) {
          thread.interrupt();

          try {
            thread.join();
          } catch (InterruptedException e) {
            log.log(Level.FINE, e.getMessage(), e);
            Thread.currentThread().interrupt();
          }
        }
      }

      final Constructor<?> ctor = clazz.getConstructor();
      ctor.setAccessible(true);
      env.set(null, ctor.newInstance());
    } catch (ReflectiveOperationException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private void hookupToGamepad(final Gamepad pad) {
    for (final Map.Entry<String, Collection<GamepadPollListener>> entry : this.componentPollListeners.entrySet()) {
      for (final GamepadPollListener listener : entry.getValue()) {
        pad.onPoll(entry.getKey(), listener);
      }
    }

    for (final Map.Entry<String, Collection<GamepadPressedListener>> entry : this.componentPressedListeners.entrySet()) {
      for (final GamepadPressedListener listener : entry.getValue()) {
        pad.onPressed(entry.getKey(), listener);
      }
    }

    for (final Map.Entry<String, Collection<GamepadReleasedListener>> entry : this.componentReleasedListeners.entrySet()) {
      for (final GamepadReleasedListener listener : entry.getValue()) {
        pad.onReleased(entry.getKey(), listener);
      }
    }

    for (final GamepadPollListener listener : this.pollListeners) {
      pad.onPoll(listener);
    }

    for (final GamepadPressedListener listener : this.pressedListeners) {
      pad.onPressed(listener);
    }

    for (final GamepadReleasedListener listener : this.releasedListeners) {
      pad.onReleased(listener);
    }
  }

  private void updateGamepads() {
    this.handleHotPluggedControllers = true;
    try {
      hackTheShitOutOfJInput();
      // update plugged in gamepads
      for (int i = 0; i < ControllerEnvironment.getDefaultEnvironment().getControllers().length; i++) {
        final Controller controller = ControllerEnvironment.getDefaultEnvironment().getControllers()[i];
        final Type type = controller.getType();
        if (!type.equals(Type.GAMEPAD)) {
          continue;
        }

        final Gamepad existing = this.get(i);
        if (existing != null && existing.getName().equals(controller.getName())) {
          // already added
          continue;
        }

        // add new gamepads
        final Gamepad newGamepad = new Gamepad(i, controller);
        this.getAll().add(newGamepad);
        for (final GamepadAddedListener listener : this.gamepadAddedConsumer) {
          listener.added(newGamepad);
        }
      }
    } catch (IllegalStateException e) {
      this.hotPlugThread.interrupt();
    } finally {
      this.handleHotPluggedControllers = false;
    }
  }

  /**
   * This listener interface receives events when gamepads gets added.
   * 
   * @see GamepadManager#onAdded(GamepadAddedListener)
   */
  @FunctionalInterface
  public interface GamepadAddedListener extends EventListener {
    /**
     * Invoked when a gamepad was added.
     * 
     * @param gamepad
     *          The added gamepad.
     */
    void added(Gamepad gamepad);
  }

  /**
   * This listener interface receives events when gamepads gets removed.
   * 
   * @see GamepadManager#onAdded(GamepadAddedListener)
   */
  @FunctionalInterface
  public interface GamepadRemovedListener extends EventListener {
    /**
     * Invoked when a gamepad was removed.
     * 
     * @param gamepad
     *          The removed gamepad.
     */
    void removed(Gamepad gamepad);
  }
}
