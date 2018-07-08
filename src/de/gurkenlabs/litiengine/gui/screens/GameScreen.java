package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.Game;

public class GameScreen extends Screen {
  public GameScreen() {
    super("GAME");
  }
  
  protected GameScreen(String name) {
    super(name);
  }

  @Override
  public void render(final Graphics2D g) {
    if (Game.getEnvironment() != null) {
      Game.getEnvironment().render(g);
    }

    super.render(g);
  }
}