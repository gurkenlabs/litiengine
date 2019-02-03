package de.gurkenlabs.litiengine.environment;

/**
 * An abstract implementation of a <code>EnvironmentListener</code> that allows to only overwrite
 * individual callbacks in anonymous implementations.
 * 
 * @see EnvironmentListener
 */
public abstract class EnvironmentAdapter implements EnvironmentListener {

  @Override
  public void loaded(Environment environment) {
  }

  @Override
  public void unloaded(Environment environment) {
  }

  @Override
  public void cleared(Environment environment) {
  }

  @Override
  public void initialized(Environment environment) {
  }
}
