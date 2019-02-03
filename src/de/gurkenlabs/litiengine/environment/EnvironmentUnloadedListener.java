package de.gurkenlabs.litiengine.environment;

import java.util.EventListener;

/**
 * This listener provides callbacks for when an <code>Environment</code> was unloaded.
 * 
 * @see Environment#load()
 */
public interface EnvironmentUnloadedListener extends EventListener {

  /**
   * This method is called after the environment was unloaded.
   * 
   * @param environment
   *          The environment that was loaded.
   */
  public void unloaded(Environment environment);
}
