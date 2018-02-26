package de.gurkenlabs.litiengine.input;

import java.util.function.Consumer;

import de.gurkenlabs.litiengine.ILaunchable;

public interface IGamepadManager extends ILaunchable, IGamepadEvents {
  public void onGamepadAdded(Consumer<IGamepad> cons);

  public void onGamepadRemoved(Consumer<IGamepad> cons);

  public void remove(IGamepad gamepad);

}