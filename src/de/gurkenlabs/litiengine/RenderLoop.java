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
  private final List<IRenderable> renderables;
  private final IRenderComponent component;
  private final ICameraProvider cameraProvider;
  /** The game is running. */
  private boolean gameIsRunning = true;

  public RenderLoop(IRenderComponent component, ICameraProvider provider) {
    this.renderables = new CopyOnWriteArrayList<>();
    this.component = component;
    this.cameraProvider = provider;
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
      for (IRenderable render : this.renderables) {
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

  public void register(IRenderable render) {
    this.renderables.add(render);
  }

  public void unregister(IRenderable render) {
    this.renderables.remove(render);
  }
}
