package de.gurkenlabs.litiengine.environment;

/**
 * An abstract implementation of a <code>EnvironmentListener</code> that allows to only overwrite
 * individual callbacks in anonymous implementations.
 * 
 * @see EnvironmentListener
 */
public abstract class EnvironmentAdapter implements EnvironmentListener {

  @Override
  public void environmentLoaded(Environment environment) {
  }

  @Override
  public void environmentUnloaded(Environment environment) {
  }

  @Override
  public void environmentCleared(Environment environment) {
  }

  @Override
  public void environmentInitialized(Environment environment) {
  }
}
