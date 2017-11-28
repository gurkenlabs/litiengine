package de.gurkenlabs.litiengine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.graphics.IRenderComponent;
import de.gurkenlabs.litiengine.graphics.IRenderable;

/**
 * The Class RenderLoop.
 */
public class RenderLoop extends Thread implements ILoop {
  private final IRenderComponent component;
  /** The game is running. */
  private boolean gameIsRunning = true;
  private final List<IRenderable> renderables;
  private final List<IUpdateable> updatables;

  private int maxFps;

  public RenderLoop(final IRenderComponent component) {
    this.renderables = new CopyOnWriteArrayList<>();
    this.updatables = new CopyOnWriteArrayList<>();

    this.component = component;
    this.maxFps = Game.getConfiguration().client().getMaxFps();
  }

  @Override
  public void attach(final IUpdateable updatable) {
    if (updatable == null) {
      return;
    }

    if (this.updatables.contains(updatable)) {
      return;
    }

    this.updatables.add(updatable);
  }

  @Override
  public void detach(final IUpdateable updatable) {
    this.updatables.remove(updatable);
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
        this.updatables.forEach(updatable -> {
          if (updatable != null) {
            updatable.update();
          }
        });

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
