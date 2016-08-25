package de.gurkenlabs.litiengine;

/**
 * The Class RenderLoop.
 */
public class RenderLoop extends Thread {

  /** The game is running. */
  private boolean gameIsRunning = true;

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
      Game.getScreenManager().renderCurrentScreen();

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
}
