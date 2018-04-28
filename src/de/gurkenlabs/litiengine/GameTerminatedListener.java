package de.gurkenlabs.litiengine;

import java.util.EventListener;

/**
 * This listener provides callbacks for when the <code>Game</code> gets terminated.
 */
public interface GameTerminatedListener extends EventListener {

  /**
   * This method is called when the <code>Game</code> was terminated (just before <code>System.exit</code> is about to be called).
   */
  public void terminated();
}
