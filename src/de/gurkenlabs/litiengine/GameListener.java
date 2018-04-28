package de.gurkenlabs.litiengine;

/**
 * This listener provides callbacks for when the <code>Game</code> gets started or terminated.
 */
public interface GameListener extends GameTerminatedListener {

  /**
   * This method gets called after the <code>Game.start</code> method was executed.
   */
  public void started();

  /**
   * This method gets called before the <code>Game</code> is about to be terminated.
   * Returning false prevents the terminate event to continue.
   *
   * @return Return false to interrupt the termination process.
   */
  public boolean terminating();
}
