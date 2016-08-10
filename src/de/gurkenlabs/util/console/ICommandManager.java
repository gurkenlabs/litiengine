package de.gurkenlabs.util.console;

import java.util.function.Function;

import de.gurkenlabs.core.ILaunchable;

public interface ICommandManager extends ILaunchable {
  public void bind(String string, Function<String[], Boolean> commandConsumer);

  public boolean executeCommand(String command);
}
