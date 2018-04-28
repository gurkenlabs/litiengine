package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * An abstract implementation of a <code>EnvironmentEntityListener</code> that allows to only overwrite
 * individual callbacks in anonymous implementations.
 * 
 * @see EnvironmentEntityListener
 */
public abstract class EnvironmentEntityAdapter implements EnvironmentEntityListener {

  @Override
  public void entityAdded(IEntity entity) {
  }

  @Override
  public void entityRemoved(IEntity entity) {
  }
}
