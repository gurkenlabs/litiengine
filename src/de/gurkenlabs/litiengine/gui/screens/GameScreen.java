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
  // The camera this game screen uses to render the world
  private int cameraIndex;

  public GameScreen() {
    this("GAME");
  }

  protected GameScreen(String name) {
    this(name, 0, 0);
  }
  protected GameScreen(String name, int layer, int cameraIndex) {
    super(name, layer);
    this.setCameraIndex(cameraIndex);
  }

  @Override
  public void render(final Graphics2D g) {
    Game.world().setActiveCameraIndex(cameraIndex);
    if (Game.world().environment() != null) {
      Game.world().environment().render(g);
    }

    super.render(g);
  }

  public int getCameraIndex() {
    return cameraIndex;
  }

  public void setCameraIndex(int cameraIndex) {
    this.cameraIndex = cameraIndex;
  }
}