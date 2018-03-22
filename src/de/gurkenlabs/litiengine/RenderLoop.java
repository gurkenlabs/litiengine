package de.gurkenlabs.litiengine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.graphics.IRenderComponent;
import de.gurkenlabs.litiengine.graphics.IRenderable;

public class RenderLoop extends UpdateLoop {
  private static final Logger log = Logger.getLogger(RenderLoop.class.getName());
  private final IRenderComponent component;
  private final List<IRenderable> renderables;

  private boolean gameIsRunning = true;
  private int maxFps;

  public RenderLoop(final IRenderComponent component) {
    super();
    this.renderables = new CopyOnWriteArrayList<>();
    this.component = component;
    this.maxFps = Game.getConfiguration().client().getMaxFps();
  }

  public void register(final IRenderable render) {
    this.renderables.add(render);
  }

  @Override
  public void run() {
    while (this.gameIsRunning) {
      final long fpsWait = (long) (1.0 / this.maxFps * 1000);
      final long renderStart = System.nanoTime();
      try {
        Game.getCamera().updateFocus();
        this.update();

        for (final IRenderable render : this.renderables) {
          this.component.render(render);
        }

        final long renderTime = (System.nanoTime() - renderStart) / 1000000;

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
