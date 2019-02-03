package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.environment.Environment;

/**
 * An abstract implementation of a <code>EntityListener</code> that allows to only overwrite
 * individual callbacks in anonymous implementations.
 * 
 * @see EntityTransformListener
 */
public abstract class EntityAdapter implements EntityListener {

  @Override
  public void loaded(IEntity entity, Environment environment) {
  }

  @Override
  public void removed(IEntity entity, Environment environment) {
  }
}
