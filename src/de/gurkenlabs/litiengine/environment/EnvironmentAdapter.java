package de.gurkenlabs.litiengine.environment;

/**
 * An abstract implementation of a <code>EnvironmentListener</code> that allows to only overwrite
 * individual callbacks in anonymous implementations.
 * 
 * @see EnvironmentListener
 */
public abstract class EnvironmentAdapter implements EnvironmentListener {

  @Override
  public void environmentLoaded(IEnvironment environment) {
  }

  @Override
  public void environmentUnloaded(IEnvironment environment) {
  }

  @Override
  public void environmentCleared(IEnvironment environment) {
  }

  @Override
  public void environmentInitialized(IEnvironment environment) {
  }
}
