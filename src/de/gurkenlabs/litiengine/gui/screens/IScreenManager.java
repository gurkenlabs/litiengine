/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.ICameraProvider;
import de.gurkenlabs.litiengine.graphics.RenderComponent;

/**
 * The screen manager manages all screens of a game. The method
 * {@link #renderCurrentScreen()} is called from the render loop of the game and
 * renders the current screen to the {@link #getRenderComponent()} of this
 * manager.
 */
public interface IScreenManager extends ICameraProvider {
  public void addScreen(final IScreen screen);

  public void displayScreen(IScreen screen);

  /**
   * Change screen.
   *
   * @param type
   *          the type
   */
  public void displayScreen(String screenName);

  public Rectangle getBounds();

  /**
   * Gets the current screen.
   *
   * @return the current screen
   */
  public IScreen getCurrentScreen();

  public RenderComponent getRenderComponent();

  /**
   * Gets the frame size.
   *
   * @return the frame size
   */
  public Dimension getResolution();

  public Point getScreenLocation();

  public String getTitle();

  public void init(int width, int height, boolean fullscreen);

  public boolean isFocusOwner();

  public void onResolutionChanged(Consumer<Dimension> resolutionConsumer);

  public void onScreenChanged(Consumer<IScreen> screenConsumer);

  public void setIconImage(Image image);

  public void setTitle(String string);
}
