package de.gurkenlabs.litiengine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.graphics.ICameraProvider;
import de.gurkenlabs.litiengine.graphics.IRenderComponent;
import de.gurkenlabs.litiengine.graphics.IRenderable;

/**
 * The Class RenderLoop.
 */
public class RenderLoop extends Thread {
  private final ICameraProvider cameraProvider;
  private final IRenderComponent component;
  /** The game is running. */
  private boolean gameIsRunning = true;
  private final List<IRenderable> renderables;

  public RenderLoop(final IRenderComponent component, final ICameraProvider provider) {
    this.renderables = new CopyOnWriteArrayList<>();
    this.component = component;
    this.cameraProvider = provider;
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
    final long FPS_WAIT = (long) (1.0 / Game.getConfiguration().CLIENT.getMaxFps() * 1000);
    while (this.gameIsRunning) {
      final long renderStart = System.nanoTime();
      this.cameraProvider.getCamera().updateFocus();
      for (final IRenderable render : this.renderables) {
        this.component.render(render);
      }

      final long renderTime = (System.nanoTime() - renderStart) / 1000000;
      try {
        Thread.sleep(Math.max(0, FPS_WAIT - renderTime));
      } catch (final InterruptedException e) {
        Thread.interrupted();
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
}
