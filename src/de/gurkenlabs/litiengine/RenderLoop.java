package de.gurkenlabs.litiengine;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.util.TimeUtilities;

public class RenderLoop extends UpdateLoop {
  private static final Logger log = Logger.getLogger(RenderLoop.class.getName());

  private boolean gameIsRunning = true;
  private int maxFps;

  public RenderLoop() {
    super();
    this.maxFps = Game.getConfiguration().client().getMaxFps();
  }

  @Override
  public void run() {
    while (this.gameIsRunning) {
      final long fpsWait = (long) (1.0 / this.maxFps * 1000);
      final long renderStart = System.nanoTime();
      try {
        Game.getCamera().updateFocus();
        this.update();

        Game.getScreenManager().getRenderComponent().render();

        final long renderTime = (long) TimeUtilities.nanoToMs(System.nanoTime() - renderStart);

        long wait = Math.max(0, fpsWait - renderTime);
        if (wait != 0) {
          Thread.sleep(wait);
        }
      } catch (final InterruptedException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  @Override
  public void terminate() {
    this.gameIsRunning = false;
  }

  public int getMaxFps() {
    return maxFps;
  }

  public void setMaxFps(int maxFps) {
    this.maxFps = maxFps;
  }
}
