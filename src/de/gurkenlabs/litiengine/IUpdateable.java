package de.gurkenlabs.litiengine;

/**
 * The Interface IUpdateable provides the functionality to update an instance
 * from the game loop. The instance needs to register itself.
 */
public interface IUpdateable {

  /**
   * This method is called by the game loop on all objects that need to update
   * their attributes. It is called on every tick, means, it is called
   * Game.GameLoop.TICKS_PER_SECOND times per second.
   * 
   * @param loop
   *          The actual game loop
   */
  public void update(final IGameLoop loop);
}
