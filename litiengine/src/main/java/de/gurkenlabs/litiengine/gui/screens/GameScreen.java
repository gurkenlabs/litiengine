package de.gurkenlabs.litiengine.gui.screens;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.graphics.ICamera;
import java.awt.Graphics2D;

/**
 * A default screen implementation that renders the game's current environment.
 *
 * @see GameWorld#environment()
 */
public class GameScreen extends Screen {
  private int cameraIndex = 0;

  public GameScreen() {
    super("GAME");
  }

  protected GameScreen(String name) {
    super(name);
  }

  /**
   * Creates a new {@code GameScreen} with the specified name and camera index.
   *
   * @param name        The name of the screen.
   * @param cameraIndex The index of the camera to use for rendering this screen.
   */
  public GameScreen(String name, int cameraIndex) {
    super(name);
    this.setCameraIndex(cameraIndex);
  }

  /**
   * Gets the index of the camera used by this screen.
   *
   * @return The camera index.
   */
  public int getCameraIndex() {
    return this.cameraIndex;
  }

  /**
   * Sets the camera index for this screen. Negative indices default to 0.
   *
   * @param cameraIndex The camera index to set.
   */
  public void setCameraIndex(int cameraIndex) {
    if (cameraIndex < 0) {
      this.cameraIndex = 0;
    } else {
      this.cameraIndex = cameraIndex;
    }
  }

  /**
   * Gets the {@code ICamera} associated with this screen.
   *
   * @return The camera for this screen, or the default camera if the index is out of bounds.
   */
  public ICamera getCamera() {
    ICamera cam = Game.world().camera(this.cameraIndex);
    return cam != null ? cam : Game.world().camera();
  }

  @Override
  public void render(final Graphics2D g) {
    int previousCameraIndex = Game.world().currentCameraIndex();
    boolean switchedCamera = Game.world().camera(this.cameraIndex) != null;
    try {
      if (switchedCamera) {
        Game.world().setCurrentCameraIndex(this.cameraIndex);
      }
      if (Game.world().environment() != null) {
        Game.world().environment().render(g);
      }
      super.render(g);
    } finally {
      if (switchedCamera) {
        Game.world().setCurrentCameraIndex(previousCameraIndex);
      }
    }
  }
}
