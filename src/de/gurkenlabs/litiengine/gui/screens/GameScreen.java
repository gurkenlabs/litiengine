package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.gui.GameRenderComponent;

/**
 * A default screen implementation that renders the game's current environment.
 * 
 * @see GameWorld#environment()
 */
public class GameScreen extends Screen {
  // The camera this game screen uses to render the world
  private int cameraIndex;
  GameRenderComponent gameRender;

  public GameScreen() {
    this("GAME");
  }

  protected GameScreen(String name) {
    super(name);

    gameRender = new GameRenderComponent(0, 0, Game.window().getWidth(), Game.window().getHeight());
    getComponents().add(gameRender);
  }
}