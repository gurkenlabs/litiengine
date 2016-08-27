package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.ScreenInfo;

@ScreenInfo(name = "GAME")
public class GameScreen extends Screen {
  public GameScreen() {
    super();
  }

  @Override
  public void render(final Graphics2D g) {
    if (Game.getEnvironment() != null) {
      Game.getEnvironment().render(g);
    }
    
    super.render(g);
  }

  @Override
  protected void initializeComponents() {
  }

}