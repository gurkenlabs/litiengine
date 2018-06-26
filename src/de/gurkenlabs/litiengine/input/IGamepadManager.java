package de.gurkenlabs.litiengine.input;

import java.util.function.Consumer;

import de.gurkenlabs.litiengine.ILaunchable;

public interface IGamepadManager extends ILaunchable, IGamepadEvents {
  public void onGamepadAdded(Consumer<Gamepad> cons);

  public void onGamepadRemoved(Consumer<Gamepad> cons);

  public void remove(Gamepad gamepad);

}