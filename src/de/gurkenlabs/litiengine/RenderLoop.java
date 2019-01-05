package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.util.TimeUtilities;

public class RenderLoop extends UpdateLoop {

  private int maxFps;

  public RenderLoop(String name) {
    super(name);
    this.maxFps = Game.config().client().getMaxFps();
  }

  @Override
  public void run() {
    while (!interrupted()) {
      final long fpsWait = (long) (1000.0 / this.maxFps);
      final long renderStart = System.nanoTime();
      try {
        Game.world().camera().updateFocus();
        this.update();

        Game.window().getRenderComponent().render();

        final double renderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);

        Game.metrics().setEstimatedMaxFramesPerSecond((int) (1000.0 / renderTime));
        if (Game.config().debug().trackRenderTimes()) {
          Game.metrics().trackRenderTime("total", renderTime);
        }

        long wait = Math.max(0, fpsWait - (long) renderTime);
        if (wait != 0) {
          sleep(wait);
        }
      } catch (final InterruptedException e) {
        interrupt();
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
