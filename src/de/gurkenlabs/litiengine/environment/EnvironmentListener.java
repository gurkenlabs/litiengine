package de.gurkenlabs.litiengine.environment;

/**
 * This listener provides callbacks for different points over the life cycle of an <code>IEnvironment</code> (loaded/unloaded/cleared/initialized).
 * 
 * @see IEnvironment#load()
 * @see IEnvironment#unload()
 * @see IEnvironment#clear()
 * @see IEnvironment#init()
 */
public interface EnvironmentListener extends EnvironmentLoadedListener {
  /**
   * This method was called after the environment was unloaded.
   * 
   * @param environment
   *          The environment that was unloaded.
   */
  public void environmentUnloaded(IEnvironment environment);

  /**
   * This method was called after the environment was cleared.
   * 
   * @param environment
   *          The environment that was cleared.
   */
  public void environmentCleared(IEnvironment environment);

  /**
   * This method was called after the environment was initialized.
   * 
   * @param environment
   *          The environment that was initialized.
   */
  public void environmentInitialized(IEnvironment environment);
}
