package de.gurkenlabs.litiengine.environment;

import java.util.EventListener;

/**
 * This listener provides callbacks for when an <code>IEnvironment</code> was unloaded.
 * 
 * @see IEnvironment#load()
 */
public interface EnvironmentUnloadedListener extends EventListener {

  /**
   * This method is called after the environment was unloaded.
   * 
   * @param environment
   *          The environment that was loaded.
   */
  public void environmentUnloaded(IEnvironment environment);
}
