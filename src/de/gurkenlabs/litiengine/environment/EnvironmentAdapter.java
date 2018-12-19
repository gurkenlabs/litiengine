package de.gurkenlabs.litiengine.environment;

/**
 * An abstract implementation of a <code>EnvironmentListener</code> that allows to only overwrite
 * individual callbacks in anonymous implementations.
 * 
 * @see EnvironmentListener
 */
public abstract class EnvironmentAdapter implements EnvironmentListener {

  @Override
  public void loaded(IEnvironment environment) {
  }

  @Override
  public void unloaded(IEnvironment environment) {
  }

  @Override
  public void cleared(IEnvironment environment) {
  }

  @Override
  public void initialized(IEnvironment environment) {
  }
}
