package de.gurkenlabs.litiengine;

public interface ILoop {
  /**
   * Attaches the update method of the specified IUpdatable instance to be
   * called every tick. The tick rate can be configured in the client
   * configuration and is independant from rendering.
   * 
   * @param updatable
   */
  public void attach(final IUpdateable updatable);

  /**
   * Detaches the specified instance from the game loop.
   * 
   * @param updatable
   */
  public void detach(final IUpdateable updatable);
}
