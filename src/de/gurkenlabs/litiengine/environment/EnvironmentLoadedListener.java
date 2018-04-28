package de.gurkenlabs.litiengine.environment;

/**
 * This listener provides callbacks for when an <code>IEnvironment</code> was loaded.
 * 
 * @see IEnvironment#load()
 */
public interface EnvironmentLoadedListener {

  /**
   * This method was called after the environment was loaded.
   * 
   * @param environment
   *          The environment that was loaded.
   */
  public void environmentLoaded(IEnvironment environment);
}
