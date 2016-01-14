package de.gurkenlabs.litiengine;

import java.util.function.Consumer;

import de.gurkenlabs.annotation.GameInfo;
import de.gurkenlabs.core.IInitializable;
import de.gurkenlabs.core.ILaunchable;
import de.gurkenlabs.litiengine.configuration.GameConfiguration;
import de.gurkenlabs.litiengine.core.IGameLoop;
import de.gurkenlabs.litiengine.gui.screens.IScreenManager;

public interface IGame extends IGameLoop, IInitializable, ILaunchable {
  public GameInfo getInfo();

  public GameConfiguration getConfiguration();

  /**
   * Gets the screen manager.
   *
   * @return the screen manager
   */
  public IScreenManager getScreenManager();

  public void onUpsChanged(Consumer<Integer> upsConsumer);

  public long getTicks();
}
