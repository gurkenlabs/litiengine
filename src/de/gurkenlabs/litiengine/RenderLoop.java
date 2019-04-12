package de.gurkenlabs.litiengine;

public class RenderLoop extends UpdateLoop {

  public RenderLoop(String name) {
    super(name, Game.config().client().getMaxFps());
  }

  public int getMaxFps() {
    return this.getTickRate();
  }

  public void setMaxFps(int maxFps) {
    this.setTickRate(Math.max(1, maxFps));
  }

  /**
   * In addition to the normal base implementation, the <code>RenderLoop</code> performs the actual rendering
   * on the UI component and tracks some detailed metrics.
   */
  @Override
  protected void process() {
    Game.world().camera().updateFocus();
    super.process();
    Game.window().getRenderComponent().render();

    this.trackRenderMetric();
  }

  private void trackRenderMetric() {
    Game.metrics().setEstimatedMaxFramesPerSecond((int) (1000.0 / this.getProcessTime()));
    if (Game.config().debug().trackRenderTimes()) {
      Game.metrics().trackRenderTime("total", this.getProcessTime());
    }
  }
}
