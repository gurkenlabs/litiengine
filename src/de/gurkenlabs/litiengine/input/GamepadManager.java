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
  private final Map<String, List<Consumer<Float>>> pollConsumer;
  private final Map<String, List<Consumer<Float>>> pressedConsumer;
  private final List<Consumer<IGamepad>> gamepadRemovedConsumer;
  private final List<Consumer<IGamepad>> gamepadAddedConsumer;

  private int defaultgamePadIndex = -1;
  private final IGameLoop loop;

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

    this.onGamepadAdded(pad -> {
      if (defaultgamePadIndex == -1) {
        this.defaultgamePadIndex = pad.getIndex();
        this.hookupToGamepad(pad);
      }
    });

    this.onGamepadRemoved(pad -> {
      if (defaultgamePadIndex == pad.getIndex()) {
        this.defaultgamePadIndex = -1;
        IGamepad newGamePad = Input.getGamepad();
        if (newGamePad != null) {
          this.defaultgamePadIndex = newGamePad.getIndex();
          this.hookupToGamepad(newGamePad);
        }
      }
    });
  }

  private void hookupToGamepad(IGamepad pad) {
    for (String ident : this.pollConsumer.keySet()) {
      for (Consumer<Float> cons : this.pollConsumer.get(ident)) {
        pad.onPoll(ident, cons);
      }
    }

    for (String ident : this.pressedConsumer.keySet()) {
      for (Consumer<Float> cons : this.pressedConsumer.get(ident)) {
        pad.onPressed(ident, cons);
      }
    }
  }

  @Override
  public void update(IGameLoop loop) {
    updateGamepads(loop);
  }

  @Override
  public void remove(IGamepad gamepad) {
    if (gamepad == null) {
      return;
    }

    Input.GAMEPADS.remove(gamepad);
    for (Consumer<IGamepad> cons : this.gamepadRemovedConsumer) {
      cons.accept(gamepad);
    }
  }

  @Override
  public void onGamepadRemoved(Consumer<IGamepad> cons) {
    this.gamepadRemovedConsumer.add(cons);
  }

  @Override
  public void onGamepadAdded(Consumer<IGamepad> cons) {
    this.gamepadAddedConsumer.add(cons);
  }

  private void updateGamepads(IGameLoop loop) {
    if (loop.getTicks() % GAMEPAD_UPDATE_DELAY != 0) {
      return;
    }

    this.hackTheShitOutOfJInputBecauseItSucks_HARD();
    // update plugged in gamepads
    for (int i = 0; i < ControllerEnvironment.getDefaultEnvironment().getControllers().length; i++) {
      Controller controller = ControllerEnvironment.getDefaultEnvironment().getControllers()[i];
      Type type = controller.getType();
      if (!type.equals(Type.GAMEPAD)) {
        continue;
      }

      IGamepad existing = Input.getGamepad(i);
      if (existing != null && existing.getName().equals(controller.getName())) {
        // already added
        continue;
      }

      // add new gamepads
      IGamepad newGamepad = new Gamepad(i, controller);
      Input.GAMEPADS.add(newGamepad);
      for (Consumer<IGamepad> cons : this.gamepadAddedConsumer) {
        cons.accept(newGamepad);
      }
    }
  }

  /**
   * In JInput it is not possible to get newly added controllers or detached
   * controllers because it will never update its controllers. If you would
   * restart the application it would work... so we just reset the environment
   * via reflection and it'll do it ;).
   */
  private void hackTheShitOutOfJInputBecauseItSucks_HARD() {
    try {
      Field env = ControllerEnvironment.class.getDeclaredField("defaultEnvironment");
      env.setAccessible(true);
      Class<?> clazz = Class.forName("net.java.games.input.DefaultControllerEnvironment");

      // kill threads that might still be running.
      // otherwise we would spawn a new thread every time this method is called
      // without killing the last one
      Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
      for (Thread thread : threadSet) {
        String name = thread.getClass().getName();
        if (name.equals("net.java.games.input.RawInputEventQueue$QueueThread")) {
          thread.interrupt();
          try {
            thread.join();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

        }
      }

      Constructor<?> ctor = clazz.getConstructor();
      ctor.setAccessible(true);
      env.set(null, ctor.newInstance());
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onPoll(String identifier, Consumer<Float> consumer) {
    String contains = null;
    for (String id : this.pollConsumer.keySet()) {
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
  public void onPressed(String identifier, Consumer<Float> consumer) {
    String contains = null;
    for (String id : this.pressedConsumer.keySet()) {
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
}
