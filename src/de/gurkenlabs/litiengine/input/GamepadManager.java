package de.gurkenlabs.litiengine.input;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameLoop;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.java.games.input.ControllerEnvironment;

public class GamepadManager implements IGamepadManager, IUpdateable {
  private static final int GAMEPAD_UPDATE_DELAY = 150;
  private int defaultgamePadIndex = -1;
  private final List<Consumer<IGamepad>> gamepadAddedConsumer;
  private final List<Consumer<IGamepad>> gamepadRemovedConsumer;
  private final IGameLoop loop;

  private final Map<String, List<Consumer<Float>>> pollConsumer;
  private final Map<String, List<Consumer<Float>>> pressedConsumer;

  public GamepadManager() {
    this.loop = new GameLoop(30);
    this.gamepadRemovedConsumer = new CopyOnWriteArrayList<>();
    this.gamepadAddedConsumer = new CopyOnWriteArrayList<>();
    this.pollConsumer = new ConcurrentHashMap<>();
    this.pressedConsumer = new ConcurrentHashMap<>();
    this.loop.attach(this);
    Game.onTerminating(s -> {
      this.loop.terminate();
      return true;
    });

    this.loop.start();

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
    String contains = null;
    for (final String id : this.pollConsumer.keySet()) {
      if (id.equals(identifier)) {
        contains = id;
        break;
      }
    }

    if (contains == null) {
      this.pollConsumer.put(identifier, new ArrayList<>());
    }

    this.pollConsumer.get(contains != null ? contains : identifier).add(consumer);
  }

  @Override
  public void onPressed(final String identifier, final Consumer<Float> consumer) {
    String contains = null;
    for (final String id : this.pressedConsumer.keySet()) {
      if (id.equals(identifier)) {
        contains = id;
        break;
      }
    }

    if (contains == null) {
      this.pressedConsumer.put(identifier, new ArrayList<>());
    }

    this.pressedConsumer.get(contains != null ? contains : identifier).add(consumer);
  }

  @Override
  public void remove(final IGamepad gamepad) {
    if (gamepad == null) {
      return;
    }

    Input.GAMEPADS.remove(gamepad);
    for (final Consumer<IGamepad> cons : this.gamepadRemovedConsumer) {
      cons.accept(gamepad);
    }
  }

  @Override
  public void update(final IGameLoop loop) {
    this.updateGamepads(loop);
  }

  /**
   * In JInput it is not possible to get newly added controllers or detached
   * controllers because it will never update its controllers. If you would
   * restart the application it would work... so we just reset the environment
   * via reflection and it'll do it ;).
   */
  private void hackTheShitOutOfJInputBecauseItSucks_HARD() {
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
          } catch (final InterruptedException e) {
            e.printStackTrace();
          }

        }
      }

      final Constructor<?> ctor = clazz.getConstructor();
      ctor.setAccessible(true);
      env.set(null, ctor.newInstance());
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  private void hookupToGamepad(final IGamepad pad) {
    for (final String ident : this.pollConsumer.keySet()) {
      for (final Consumer<Float> cons : this.pollConsumer.get(ident)) {
        pad.onPoll(ident, cons);
      }
    }

    for (final String ident : this.pressedConsumer.keySet()) {
      for (final Consumer<Float> cons : this.pressedConsumer.get(ident)) {
        pad.onPressed(ident, cons);
      }
    }
  }

  private void updateGamepads(final IGameLoop loop) {
    if (loop.getTicks() % GAMEPAD_UPDATE_DELAY != 0) {
      return;
    }

    this.hackTheShitOutOfJInputBecauseItSucks_HARD();
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
      Input.GAMEPADS.add(newGamepad);
      for (final Consumer<IGamepad> cons : this.gamepadAddedConsumer) {
        cons.accept(newGamepad);
      }
    }
  }
}
