package de.gurkenlabs.litiengine.util;

import java.util.function.Function;

import de.gurkenlabs.litiengine.ILaunchable;

public interface ICommandManager extends ILaunchable {
  public void bind(String string, Function<String[], Boolean> commandConsumer);

  public boolean executeCommand(String command);
}
