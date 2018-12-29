package de.gurkenlabs.litiengine.input;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameAdapter;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.java.games.input.ControllerEnvironment;

public class GamepadManager implements IGamepadManager {
  private static final Logger log = Logger.getLogger(GamepadManager.class.getName());

  private final List<Consumer<IGamepad>> gamepadAddedConsumer;
  private final List<Consumer<IGamepad>> gamepadRemovedConsumer;

  private final Map<String, List<Consumer<Float>>> componentPollConsumer;
  private final Map<String, List<Consumer<Float>>> componentPressedConsumer;
  private final Map<String, List<Consumer<Float>>> componentReleasedConsumer;
  private final List<BiConsumer<String, Float>> pollConsumer;
  private final List<BiConsumer<String, Float>> pressedConsumer;
  private final List<BiConsumer<String, Float>> releasedConsumer;

  private final Thread hotPlugThread;

  private int defaultgamePadIndex = -1;
  private boolean handleHotPluggedControllers;

  public GamepadManager() {
    this.gamepadRemovedConsumer = new CopyOnWriteArrayList<>();
    this.gamepadAddedConsumer = new CopyOnWriteArrayList<>();
    this.componentPollConsumer = new ConcurrentHashMap<>();
    this.componentPressedConsumer = new ConcurrentHashMap<>();
    this.componentReleasedConsumer = new ConcurrentHashMap<>();
    this.pollConsumer = new CopyOnWriteArrayList<>();
    this.pressedConsumer = new CopyOnWriteArrayList<>();
    this.releasedConsumer = new CopyOnWriteArrayList<>();

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

    Game.addGameListener(new GameAdapter() {
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
        final IGamepad newGamePad = Input.getGamepad();
        if (newGamePad != null) {
          this.defaultgamePadIndex = newGamePad.getIndex();
          this.hookupToGamepad(newGamePad);
        }
      }
    });
  }

  @Override
  public void onGamepadAdded(final Consumer<IGamepad> cons) {
    this.gamepadAddedConsumer.add(cons);
  }

  @Override
  public void onGamepadRemoved(final Consumer<IGamepad> cons) {
    this.gamepadRemovedConsumer.add(cons);
  }

  @Override
  public void onPoll(final String identifier, final Consumer<Float> consumer) {
    addComponentConsumer(this.componentPollConsumer, identifier, consumer);
  }

  @Override
  public void onPoll(BiConsumer<String, Float> consumer) {
    if (this.pollConsumer.contains(consumer)) {
      return;
    }

    this.pollConsumer.add(consumer);
  }

  @Override
  public void onPressed(final String identifier, final Consumer<Float> consumer) {
    addComponentConsumer(this.componentPressedConsumer, identifier, consumer);
  }

  @Override
  public void onPressed(BiConsumer<String, Float> consumer) {
    if (this.pressedConsumer.contains(consumer)) {
      return;
    }

    this.pressedConsumer.add(consumer);
  }

  @Override
  public void onReleased(String identifier, Consumer<Float> consumer) {
    addComponentConsumer(this.componentReleasedConsumer, identifier, consumer);
  }

  @Override
  public void onReleased(BiConsumer<String, Float> consumer) {
    if (this.releasedConsumer.contains(consumer)) {
      return;
    }

    this.releasedConsumer.add(consumer);
  }

  @Override
  public void remove(final IGamepad gamepad) {
    if (gamepad == null) {
      return;
    }

    Input.gamepads().remove(gamepad);
    for (final Consumer<IGamepad> cons : this.gamepadRemovedConsumer) {
      cons.accept(gamepad);
    }
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

  protected static void addComponentConsumer(Map<String, List<Consumer<Float>>> consumerList, String identifier, Consumer<Float> consumer) {
    if (!consumerList.containsKey(identifier)) {
      consumerList.put(identifier, new ArrayList<>());
    }

    consumerList.get(identifier).add(consumer);
  }

  /**
   * In JInput it is not possible to get newly added controllers or detached
   * controllers because it will never update its controllers. If you would
   * restart the application it would work... so we just reset the environment via
   * reflection and it'll do it ;).
   */
  private void hackTheShitOutOfJInputBecauseItSucksHard() {
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

  private void hookupToGamepad(final IGamepad pad) {
    for (final Map.Entry<String, List<Consumer<Float>>> entry : this.componentPollConsumer.entrySet()) {
      for (final Consumer<Float> cons : entry.getValue()) {
        pad.onPoll(entry.getKey(), cons);
      }
    }

    for (final Map.Entry<String, List<Consumer<Float>>> entry : this.componentPressedConsumer.entrySet()) {
      for (final Consumer<Float> cons : entry.getValue()) {
        pad.onPressed(entry.getKey(), cons);
      }
    }

    for (final Map.Entry<String, List<Consumer<Float>>> entry : this.componentReleasedConsumer.entrySet()) {
      for (final Consumer<Float> cons : entry.getValue()) {
        pad.onReleased(entry.getKey(), cons);
      }
    }

    for (final BiConsumer<String, Float> consumer : this.pollConsumer) {
      pad.onPoll(consumer);
    }

    for (final BiConsumer<String, Float> consumer : this.pressedConsumer) {
      pad.onPressed(consumer);
    }

    for (final BiConsumer<String, Float> consumer : this.releasedConsumer) {
      pad.onReleased(consumer);
    }
  }

  private void updateGamepads() {
    this.handleHotPluggedControllers = true;
    try {
      this.hackTheShitOutOfJInputBecauseItSucksHard();
      // update plugged in gamepads
      for (int i = 0; i < ControllerEnvironment.getDefaultEnvironment().getControllers().length; i++) {
        final Controller controller = ControllerEnvironment.getDefaultEnvironment().getControllers()[i];
        final Type type = controller.getType();
        if (!type.equals(Type.GAMEPAD)) {
          continue;
        }

        final IGamepad existing = Input.getGamepad(i);
        if (existing != null && existing.getName().equals(controller.getName())) {
          // already added
          continue;
        }

        // add new gamepads
        final IGamepad newGamepad = new Gamepad(i, controller);
        Input.gamepads().add(newGamepad);
        for (final Consumer<IGamepad> cons : this.gamepadAddedConsumer) {
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
