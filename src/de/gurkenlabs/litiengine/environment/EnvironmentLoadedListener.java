package de.gurkenlabs.litiengine.environment;

import java.util.EventListener;

/**
 * This listener provides callbacks for when an <code>Environment</code> was loaded.
 * 
 * @see Environment#load()
 */
public interface EnvironmentLoadedListener extends EventListener {

  /**
   * This method was called after the environment was loaded.
   * 
   * @param environment
   *          The environment that was loaded.
   */
  public void loaded(Environment environment);
}
