package de.gurkenlabs.litiengine.gui.screens;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.GuiComponent;

/**
 * Screens are the containers that allow you to organize the visible contents of your game. They render the game's
 * Environment and are considered the parent of all GUI components you want to display in a particular state of your
 * game. The screen itself inherits from GuiComponent and thereby provides support to define an Appearance and listen to
 * all kinds of Input events (e.g. {@code onMouseMoved(…)}). Everything that should be visible to the player needs to be
 * rendered to the currently active screen.
 */
public abstract class Screen extends GuiComponent {
  private int screenLayer = 0;

  protected Screen(final String screenName) {
    super(0, 0);
    this.setName(screenName);
  }

  /**
   * Creates a new {@code Screen} with the specified name and screen layer.
   *
   * @param screenName  The name of the screen.
   * @param screenLayer The rendering layer of this screen (lower values are rendered first).
   */
  protected Screen(final String screenName, int screenLayer) {
    super(0, 0);
    this.setName(screenName);
    this.screenLayer = screenLayer;
  }

  /**
   * Gets the screen layer used for ordering when multiple screens are active.
   *
   * @return The screen layer value (lower values are rendered first).
   */
  public int getScreenLayer() {
    return this.screenLayer;
  }

  /**
   * Sets the screen layer used for ordering when multiple screens are active.
   *
   * @param screenLayer The screen layer value (lower values are rendered first).
   */
  public void setScreenLayer(int screenLayer) {
    this.screenLayer = screenLayer;
    if (Game.screens() != null) {
      Game.screens().sortActiveScreens();
    }
  }
}
