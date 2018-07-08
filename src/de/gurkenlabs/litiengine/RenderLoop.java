package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.util.TimeUtilities;

public class RenderLoop extends UpdateLoop {

  private int maxFps;

  public RenderLoop() {
    super();
    this.maxFps = Game.getConfiguration().client().getMaxFps();
  }

  @Override
  public void run() {
    while (!interrupted()) {
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
        break;
      }
    }
  }

  @Override
  public void terminate() {
    interrupt();
  }

  public int getMaxFps() {
    return maxFps;
  }

  public void setMaxFps(int maxFps) {
    this.maxFps = maxFps;
  }
}
