package de.gurkenlabs.litiengine.input;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.ILaunchable;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.java.games.input.ControllerEnvironment;

public class GamepadManager implements ILaunchable, GamepadEvents {
  private static final Logger log = Logger.getLogger(GamepadManager.class.getName());

  private final Collection<Consumer<Gamepad>> gamepadAddedConsumer;
  private final Collection<Consumer<Gamepad>> gamepadRemovedConsumer;

  private final Map<String, Collection<GamepadPollListener>> componentPollListeners;
  private final Map<String, Collection<GamepadPressedListener>> componentPressedListeners;
  private final Map<String, Collection<GamepadReleasedListener>> componentReleasedListeners;
  private final Collection<GamepadPollListener> pollListeners;
  private final Collection<GamepadPressedListener> pressedListeners;
  private final Collection<GamepadReleasedListener> releasedListeners;

  private final List<Gamepad> gamePads;

  private final Thread hotPlugThread;

  private int defaultgamePadIndex = -1;
  private boolean handleHotPluggedControllers;

  public GamepadManager() {
    this.gamepadRemovedConsumer = ConcurrentHashMap.newKeySet();
    this.gamepadAddedConsumer = ConcurrentHashMap.newKeySet();
    this.componentPollListeners = new ConcurrentHashMap<>();
    this.componentPressedListeners = new ConcurrentHashMap<>();
    this.componentReleasedListeners = new ConcurrentHashMap<>();
    this.pollListeners = ConcurrentHashMap.newKeySet();
    this.pressedListeners = ConcurrentHashMap.newKeySet();
    this.releasedListeners = ConcurrentHashMap.newKeySet();

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

    this.onGamepadAdded(pad -> {
      if (this.defaultgamePadIndex == -1) {
        this.defaultgamePadIndex = pad.getIndex();
        this.hookupToGamepad(pad);
      }
    });

    this.onGamepadRemoved(pad -> {
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

  public List<Gamepad> getAll() {
    return this.gamePads;
  }

  /**
   * Gets the first gamepad that is currently available.
   *
   * @return The first available {@link Gamepad} instance
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
   */
  public Gamepad get(final int index) {
    for (final Gamepad gamepad : this.gamePads) {
      if (gamepad.getIndex() == index) {
        return gamepad;
      }
    }

    return null;
  }

  public void onGamepadAdded(final Consumer<Gamepad> cons) {
    this.gamepadAddedConsumer.add(cons);
  }

  public void onGamepadRemoved(final Consumer<Gamepad> cons) {
    this.gamepadRemovedConsumer.add(cons);
  }

  @Override
  public void onPoll(final String identifier, final GamepadPollListener consumer) {
    addComponentListener(this.componentPollListeners, identifier, consumer);
  }

  @Override
  public void removePollListener(String identifier, GamepadPollListener listener) {
    removeComponentListener(this.componentPollListeners, identifier, listener);
  }

  @Override
  public void onPressed(final String identifier, final GamepadPressedListener listener) {
    addComponentListener(this.componentPressedListeners, identifier, listener);
  }

  @Override
  public void removePressedListener(String identifier, GamepadPressedListener listener) {
    removeComponentListener(this.componentPressedListeners, identifier, listener);
  }

  @Override
  public void onReleased(String identifier, GamepadReleasedListener listener) {
    addComponentListener(this.componentReleasedListeners, identifier, listener);
  }

  @Override
  public void removeReleasedListener(String identifier, GamepadReleasedListener listener) {
    removeComponentListener(this.componentReleasedListeners, identifier, listener);
  }

  @Override
  public void onPoll(GamepadPollListener listener) {
    this.pollListeners.add(listener);
  }

  @Override
  public void removePollListener(GamepadPollListener listener) {
    this.pollListeners.remove(listener);
  }

  @Override
  public void onPressed(GamepadPressedListener listener) {
    this.pressedListeners.add(listener);
  }

  @Override
  public void removePressedListener(GamepadPressedListener listener) {
    this.pressedListeners.remove(listener);
  }

  @Override
  public void onReleased(GamepadReleasedListener listener) {
    this.releasedListeners.add(listener);
  }

  @Override
  public void removeReleasedListener(GamepadReleasedListener listener) {
    this.releasedListeners.remove(listener);
  }

  @Override
  public void clearEventListeners() {
    this.componentPollListeners.clear();
    this.componentPressedListeners.clear();
    this.componentReleasedListeners.clear();

    this.pollListeners.clear();
    this.pressedListeners.clear();
    this.releasedListeners.clear();
  }

  @Override
  public void start() {
    this.hotPlugThread.start();
  }

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

  @Override
  public boolean isPressed(String gamepadComponent) {
    return this.current() != null && this.current().isPressed(gamepadComponent);
  }

  static <T> void addComponentListener(Map<String, Collection<T>> consumerList, String identifier, T consumer) {
    if (!consumerList.containsKey(identifier)) {
      consumerList.put(identifier, new ArrayList<>());
    }

    consumerList.get(identifier).add(consumer);
  }

  static <T> void removeComponentListener(Map<String, Collection<T>> consumerList, String identifier, T consumer) {
    if (!consumerList.containsKey(identifier)) {
      return;
    }

    consumerList.get(identifier).remove(consumer);
  }

  void remove(final Gamepad gamepad) {
    if (gamepad == null) {
      return;
    }

    this.getAll().remove(gamepad);
    for (final Consumer<Gamepad> cons : this.gamepadRemovedConsumer) {
      cons.accept(gamepad);
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
        for (final Consumer<Gamepad> cons : this.gamepadAddedConsumer) {
          cons.accept(newGamepad);
        }
      }
    } catch (IllegalStateException e) {
      this.hotPlugThread.interrupt();
    } finally {
      this.handleHotPluggedControllers = false;
    }
  }
}
