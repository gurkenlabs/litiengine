package de.gurkenlabs.litiengine.environment;

/**
 * This listener provides callbacks for different points over the life cycle of an <code>IEnvironment</code> (loaded/unloaded/cleared/initialized).
 * 
 * @see Environment#load()
 * @see Environment#unload()
 * @see Environment#clear()
 * @see Environment#init()
 */
public interface EnvironmentListener extends EnvironmentLoadedListener, EnvironmentUnloadedListener {

  /**
   * This method was called after the environment was cleared.
   * 
   * @param environment
   *          The environment that was cleared.
   */
  public void cleared(Environment environment);

  /**
   * This method was called after the environment was initialized.
   * 
   * @param environment
   *          The environment that was initialized.
   */
  public void initialized(Environment environment);
}
