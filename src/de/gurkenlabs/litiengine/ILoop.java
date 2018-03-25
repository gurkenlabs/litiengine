package de.gurkenlabs.litiengine;

public interface ILoop extends ILaunchable {
  /**
   * Attaches the update method of the specified IUpdatable instance to be called
   * every tick. The tick rate can be configured in the client configuration and
   * is independent from rendering.
   * 
   * @param updatable
   *          The instance that will be registered for the update event.
   */
  public void attach(final IUpdateable updatable);

  /**
   * Detaches the specified instance from the game loop.
   * 
   * @param updatable The instance that will be unregistered for the update event.
   */
  public void detach(final IUpdateable updatable);
}
