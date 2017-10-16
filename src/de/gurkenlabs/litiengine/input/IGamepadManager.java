package de.gurkenlabs.litiengine.input;

import java.util.function.Consumer;

import de.gurkenlabs.core.ILaunchable;

public interface IGamepadManager extends ILaunchable {
  public void onGamepadAdded(Consumer<IGamepad> cons);

  public void onGamepadRemoved(Consumer<IGamepad> cons);

  /**
   * Allows to register for poll events on the default gamepad (first one that
   * can be found).
   * 
   * @param identifier
   * @param consumer
   */
  public void onPoll(String identifier, Consumer<Float> consumer);

  public void onPressed(String identifier, Consumer<Float> consumer);

  public void remove(IGamepad gamepad);

}