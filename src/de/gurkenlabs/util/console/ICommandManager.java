package de.gurkenlabs.util.console;

import de.gurkenlabs.core.ILaunchable;

public interface ICommandManager extends ILaunchable {
  public boolean executeCommand(String command);
}
