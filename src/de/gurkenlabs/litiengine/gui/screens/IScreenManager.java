package de.gurkenlabs.litiengine.gui.screens;

import java.util.function.Consumer;

/**
 * The screen manager manages all screens of a game. The method
 * renderCurrentScreen is called from the render loop of the game and renders
 * the current screen to the getRenderComponent() of this manager.
 */
public interface IScreenManager{
  public void addScreen(final Screen screen);

  public void displayScreen(Screen screen);

  public void displayScreen(String screenName);

  public Screen getCurrentScreen();

  public void onScreenChanged(Consumer<Screen> screenConsumer);
}
