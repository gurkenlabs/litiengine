package de.gurkenlabs.litiengine;

import java.util.function.Consumer;

import de.gurkenlabs.core.IInitializable;
import de.gurkenlabs.core.ILaunchable;
import de.gurkenlabs.litiengine.annotation.GameInfo;
import de.gurkenlabs.litiengine.configuration.GameConfiguration;
import de.gurkenlabs.litiengine.graphics.IGraphicsEngine;
import de.gurkenlabs.litiengine.gui.screens.IScreenManager;

public interface IGame extends IGameLoop, IInitializable, ILaunchable {
  public GameConfiguration getConfiguration();

  public IGraphicsEngine getGraphicsEngine();

  public GameInfo getInfo();

  /**
   * Gets the screen manager.
   *
   * @return the screen manager
   */
  public IScreenManager getScreenManager();

  public void onUpsChanged(Consumer<Integer> upsConsumer);
}
