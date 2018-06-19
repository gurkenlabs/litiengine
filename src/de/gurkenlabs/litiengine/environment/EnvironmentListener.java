package de.gurkenlabs.litiengine.environment;

/**
 * This listener provides callbacks for different points over the life cycle of an <code>Environment</code> (loaded/unloaded/cleared/initialized).
 * 
 * @see Environment#load()
 * @see Environment#unload()
 * @see Environment#clear()
 * @see Environment#init()
 */
public interface EnvironmentListener extends EnvironmentLoadedListener {
  /**
   * This method was called after the environment was unloaded.
   * 
   * @param environment
   *          The environment that was unloaded.
   */
  public void environmentUnloaded(Environment environment);

  /**
   * This method was called after the environment was cleared.
   * 
   * @param environment
   *          The environment that was cleared.
   */
  public void environmentCleared(Environment environment);

  /**
   * This method was called after the environment was initialized.
   * 
   * @param environment
   *          The environment that was initialized.
   */
  public void environmentInitialized(Environment environment);
}
