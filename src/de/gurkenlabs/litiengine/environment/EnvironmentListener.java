package de.gurkenlabs.litiengine.environment;

/**
 * This listener provides callbacks for different points over the life cycle of an {@code
 * IEnvironment} (loaded/unloaded/cleared/initialized).
 *
 * @see Environment#load()
 * @see Environment#unload()
 * @see Environment#clear()
 * @see Environment#init()
 */
public interface EnvironmentListener
    extends EnvironmentLoadedListener, EnvironmentUnloadedListener {

  /**
   * This method was called after the environment was cleared.
   *
   * @param environment The environment that was cleared.
   */
  default void cleared(Environment environment) {}

  /**
   * This method was called after the environment was initialized.
   *
   * @param environment The environment that was initialized.
   */
  default void initialized(Environment environment) {}

  @Override
  default void loaded(Environment environment) {}

  @Override
  default void unloaded(Environment environment) {}
}
