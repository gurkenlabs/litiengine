package de.gurkenlabs.litiengine.environment;

import java.util.EventListener;

/**
 * This listener provides callbacks for when an {@code Environment} was loaded.
 * 
 * @see Environment#load()
 */
@FunctionalInterface
public interface EnvironmentLoadedListener extends EventListener {

  /**
   * This method is called after the environment was loaded.
   * 
   * @param environment
   *          The environment that was loaded.
   */
  public void loaded(Environment environment);
}
