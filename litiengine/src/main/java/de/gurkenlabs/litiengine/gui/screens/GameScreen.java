package de.gurkenlabs.litiengine.gui.screens;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.graphics.ICamera;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

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
    int resolvedCameraIndex = this.getResolvedCameraIndex();
    return resolvedCameraIndex >= 0 ? Game.world().camera(resolvedCameraIndex) : null;
  }

  @Override
  public void render(final Graphics2D g) {
    ICamera renderCamera = this.getCamera();
    if (renderCamera == null) {
      super.render(g);
      return;
    }

    ICamera previousRenderCamera = Game.world().renderCamera();
    Game.world().setRenderCamera(renderCamera);
    try {
      if (Game.world().environment() != null) {
        Graphics2D environmentGraphics = (Graphics2D) g.create();
        try {
          environmentGraphics.clip(new Rectangle2D.Double(this.getX(), this.getY(), this.getWidth(), this.getHeight()));
          Game.world().environment().render(environmentGraphics);
        } finally {
          environmentGraphics.dispose();
        }
      }
      super.render(g);
    } finally {
      Game.world().setRenderCamera(previousRenderCamera);
    }
  }

  private int getResolvedCameraIndex() {
    if (Game.world().camera(this.cameraIndex) != null) {
      return this.cameraIndex;
    }

    return Game.world().camera(0) != null ? 0 : -1;
  }
}
