package de.gurkenlabs.litiengine.gui.screens;

import de.gurkenlabs.litiengine.gui.GuiComponent;

/**
 * Screens are the containers that allow you to organize the visible contents of your game. They render the game’s Environment and are considered the
 * parent of all GUI components you want to display in a particular state of your game. The screen itself inherits from GuiComponent and thereby
 * provides support to define an Appearance and listen to all kinds of Input events (e.g. <code>onMouseMoved(…)</code>). Everything that should be visible to the
 * player needs to be rendered to the currently active screen.
 * 
 */
public abstract class Screen extends GuiComponent {
  // The screen's layer determines the render order.
  // The higher the value, the later the screen will be rendered.
  // This is useful if you wish to make some sort of overlay, such as a UI.
  private int layer;

  protected Screen(final String screenName) {
    this(screenName, 0);
  }

  protected Screen(final String screenName, int layer) {
    super(0,0);
    this.setName(screenName);
    this.setLayer(layer);
  }

  public int getLayer() {
    return layer;
  }

  public void setLayer(int layer) {
    this.layer = layer;
  }
}
