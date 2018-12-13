package de.gurkenlabs.litiengine.gui.screens;

import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.RenderComponent;

/**
 * The <code>ScreenManager</code> holds instances of all available screen and handles whenever a different <code>Screen</code> should be shown to the
 * player. It provides the
 * currently active Screen for the Game’s <code>RenderComponent</code> which calls the <code>Screen.render(Graphics2D)</code> method on every tick of
 * the <code>RenderLoop</code>.
 * Overwriting this method provides the ability to define a customized render pipeline that suits the need of a particular Screen implementation. With
 * the GameScreen, the LITIengine provides a simple default Screen implementation that renders the current <code>Environment</code> and all its
 * <code>GuiComponents</code>.
 * 
 * @see Screen
 * @see RenderComponent
 * @see GameScreen
 * @see Screen#render(java.awt.Graphics2D)
 * @see Game#renderLoop()
 */
public interface IScreenManager {
  public void add(Screen screen);

  public void remove(Screen screen);

  public void display(Screen screen);

  public void display(String screenName);

  public Screen get(String screenName);

  public Screen current();

  public void onScreenChanged(Consumer<Screen> screenConsumer);
}
