package de.gurkenlabs.util;

import de.gurkenlabs.litiengine.ILaunchable;

public interface ICommandListener extends ILaunchable {
  public void register(ICommandManager manager);
}
