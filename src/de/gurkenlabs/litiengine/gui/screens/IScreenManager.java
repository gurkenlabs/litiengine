package de.gurkenlabs.litiengine.gui.screens;

import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.GameWindow;

/**
 * The screen manager manages all screens of a game. The method
 * renderCurrentScreen is called from the render loop of the game and renders
 * the current screen to the getRenderComponent() of this manager.
 */
public interface IScreenManager extends GameWindow{
  public void addScreen(final Screen screen);

  public void displayScreen(Screen screen);

  public void displayScreen(String screenName);

  public Screen getCurrentScreen();

  public void onScreenChanged(Consumer<Screen> screenConsumer);
}
