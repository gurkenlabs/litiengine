package de.gurkenlabs.litiengine.entities;

/**
 * An abstract implementation of a <code>EntityTransformListener</code> that allows to only overwrite
 * individual callbacks in anonymous implementations.
 * 
 * @see EntityTransformListener
 */
public abstract class EntityTransformAdapter implements EntityTransformListener {

  @Override
  public void locationChanged(IEntity entity) {
  }

  @Override
  public void sizeChanged(IEntity entity) {
  }
}
