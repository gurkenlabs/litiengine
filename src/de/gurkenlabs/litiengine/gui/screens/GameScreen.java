package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.GameWorld;

/**
 * A default screen implementation that renders the game's current environment.
 * 
 * @see GameWorld#environment()
 */
public class GameScreen extends Screen {
  public GameScreen() {
    super("GAME");
  }

  protected GameScreen(String name) {
    super(name);
  }

  @Override
  public void render(final Graphics2D g) {
    if (Game.world().environment() != null) {
      Game.world().environment().render(g);
    }

    super.render(g);
  }
}