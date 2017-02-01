package de.gurkenlabs.litiengine.input;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Controller.Type;

public class GamepadManager implements IGamepadManager, IUpdateable {
  private final List<Consumer<IGamepad>> gamepadRemovedConsumer;
  private final List<Consumer<IGamepad>> gamepadAddedConsumer;

  public GamepadManager() {
    this.gamepadRemovedConsumer = new CopyOnWriteArrayList<>();
    this.gamepadAddedConsumer = new CopyOnWriteArrayList<>();
    Game.getLoop().registerForUpdate(this);
  }

  @Override
  public void update(IGameLoop loop) {
    updateGamepads();
  }

  public void onGamepadRemoved(Consumer<IGamepad> cons) {
    this.gamepadRemovedConsumer.add(cons);
  }

  public void onGamepadAdded(Consumer<IGamepad> cons) {
    this.gamepadAddedConsumer.add(cons);
  }

  private void updateGamepads() {
    if (Game.getLoop().getTicks() % 150 == 0) {
      this.hackTheShitOutOfJInputBecauseItSucks_HARD();
    }

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
   * In JInput it is not possible to get newly added controllers or detached controllers because it will never update its controllers.
   * If you would restart the application it would work... so we just reset the environment via reflection and it'll do it ;).
   */
  private void hackTheShitOutOfJInputBecauseItSucks_HARD() {
    try {
      Field env = ControllerEnvironment.class.getDeclaredField("defaultEnvironment");
      env.setAccessible(true);
      Class<?> clazz = Class.forName("net.java.games.input.DefaultControllerEnvironment");
      Constructor<?> ctor = clazz.getConstructor();
      ctor.setAccessible(true);
      env.set(null, ctor.newInstance());
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
