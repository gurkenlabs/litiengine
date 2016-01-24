/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Component;
import java.awt.Dimension;
import java.util.function.Consumer;

/**
 * The screen manager manages all screens of a game. The method
 * {@link #renderCurrentScreen()} is called from the render loop of the game and
 * renders the current screen to the {@link #getRenderComponent()} of this
 * manager.
 */
public interface IScreenManager {
  public void addScreen(final IScreen screen);

  /**
   * Change screen.
   *
   * @param type
   *          the type
   */
  public void changeScreen(String screenName);

  /**
   * Gets the current screen.
   *
   * @return the current screen
   */
  public IScreen getCurrentScreen();

  public Component getRenderComponent();

  /**
   * Gets the frame size.
   *
   * @return the frame size
   */
  public Dimension getResolution();

  public void init(int width, int height, boolean fullscreen);

  public void onFpsChanged(Consumer<Integer> fpsConsumer);

  public void onResolutionChanged(Consumer<Dimension> resolutionConsumer);

  /**
   * Render the current screen.
   */
  public void renderCurrentScreen();
}
