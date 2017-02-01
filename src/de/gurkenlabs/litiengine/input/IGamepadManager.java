package de.gurkenlabs.litiengine.input;

import java.util.function.Consumer;

public interface IGamepadManager {
  public void onGamepadAdded(Consumer<IGamepad> cons);

  public void onGamepadRemoved(Consumer<IGamepad> cons);
}
