/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.ICamera;

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

  public ICamera getCamera();

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

  public Point getScreenLocation();

  public void init(int width, int height, boolean fullscreen);

  public void onFpsChanged(Consumer<Integer> fpsConsumer);

  public void onResolutionChanged(Consumer<Dimension> resolutionConsumer);

  public void onScreenChanged(Consumer<IScreen> screenConsumer);

  /**
   * Render the current screen.
   */
  public void renderCurrentScreen();

  /**
   * Sets the camera.
   *
   * @param camera
   *          the new camera
   */
  public void setCamera(ICamera camera);

  public void setIconImage(Image image);

  public void setCursor(Image image);
  
  public boolean isFocusOwner();
}
