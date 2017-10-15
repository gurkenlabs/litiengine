package de.gurkenlabs.litiengine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.graphics.IRenderComponent;
import de.gurkenlabs.litiengine.graphics.IRenderable;

/**
 * The Class RenderLoop.
 */
public class RenderLoop extends Thread {
  private final IRenderComponent component;
  /** The game is running. */
  private boolean gameIsRunning = true;
  private final List<IRenderable> renderables;

  private int maxFps;

  public RenderLoop(final IRenderComponent component) {
    this.renderables = new CopyOnWriteArrayList<>();
    this.component = component;
    this.maxFps = Game.getConfiguration().client().getMaxFps();
  }

  public void register(final IRenderable render) {
    this.renderables.add(render);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    while (this.gameIsRunning) {
      final long fpsWait = (long) (1.0 / this.maxFps * 1000);
      final long renderStart = System.nanoTime();
      try {
        Game.getCamera().updateFocus();
        for (final IRenderable render : this.renderables) {
          this.component.render(render);
        }

        final long renderTime = (System.nanoTime() - renderStart) / 1000000;

        Thread.sleep(Math.max(0, fpsWait - renderTime));
      } catch (final InterruptedException e) {
        this.interrupt();
        break;
      }
    }
  }

  /**
   * Terminate.
   */
  public void terminate() {
    this.gameIsRunning = false;
  }

  public void unregister(final IRenderable render) {
    this.renderables.remove(render);
  }

  public int getMaxFps() {
    return maxFps;
  }

  public void setMaxFps(int maxFps) {
    this.maxFps = maxFps;
  }
}
