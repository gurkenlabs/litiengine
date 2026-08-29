package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.configuration.ClientConfiguration;

/**
 * The functional interface {@code IUpdateable} provides the functionality to automatically update the instance from a
 * loop that it is attached to.
 * 
 * <p>
 * This should be used for code that needs to be executed on every tick/frame.
 * </p>
 * 
 * @see ILoop#attach(IUpdateable)
 * @see ILoop#detach(IUpdateable)
 * @see Game#loop()
 */
@FunctionalInterface
public interface IUpdateable {

  /** Default update priority. Lower priorities are updated first. */
  int DEFAULT_UPDATE_PRIORITY = 0;

  /**
   * This method is called by the game loop on all objects that are attached to the loop. It's called on every tick of the
   * loop and the frequency can be configured using the {@code ClientConfiguration}.
   *
   * @see ClientConfiguration#setMaxFps(int)
   */
  void update();

  /**
   * Determines this object's stable position in an update tick. Objects with the same priority retain their
   * registration order.
   *
   * @return the update priority; lower values run first
   */
  default int getUpdatePriority() {
    return DEFAULT_UPDATE_PRIORITY;
  }
}
