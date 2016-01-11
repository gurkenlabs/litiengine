package de.gurkenlabs.util.console;

import de.gurkenlabs.core.ILaunchable;

public interface ICommandListener extends ILaunchable{
  public void register(ICommandManager manager);
}
