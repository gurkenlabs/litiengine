package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.configuration.ClientConfiguration;

/**
 * The <code>IUpdateable</code> interface provides the functionality to automatically update the instance
 * from a loop that the it is attached to.
 * 
 * <p>
 * This should be used for code that needs to be executed on every tick/frame.
 * </p>
 * 
 * @see ILoop#attach(IUpdateable)
 * @see ILoop#detach(IUpdateable)
 * @see Game#loop()
 */
public interface IUpdateable {

  /**
   * This method is called by the game loop on all objects that are attached to the loop.
   * It's called on every tick of the loop and the frequency can be configured using the <code>ClientConfiguration</code>.
   *
   * @see ClientConfiguration#setMaxFps(int)
   */
  public void update();

  /**
   * This flag controls whether this instance is currently active and thereby needs to be updated by the game loop.
   * 
   * @return True if this instance should be updated; otherwise false.
   */
  public default boolean isActive() {
    return true;
  }
}
