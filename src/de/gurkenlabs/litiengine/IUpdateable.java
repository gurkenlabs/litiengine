package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.configuration.ClientConfiguration;

/**
 * The Interface IUpdateable provides the functionality to automatically update an instance that implements it
 * from the game loop. The instance needs to be registered on the loop.
 * 
 * @see ILoop#attach(IUpdateable)
 * @see ILoop#detach(IUpdateable)
 */
public interface IUpdateable {

  /**
   * This method is called by the game loop on all objects that are attached to the loop.
   * It's called on every tick of the loop and the frequency can be configured using the <code>ClientConfiguration</code>.
   *
   * @see ClientConfiguration#setUpdaterate(int)
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
