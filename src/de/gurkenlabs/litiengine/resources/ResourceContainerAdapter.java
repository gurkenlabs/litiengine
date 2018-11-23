package de.gurkenlabs.litiengine.resources;

/**
 * An abstract implementation of a <code>ResourcesContainerListener</code> that allows to only overwrite
 * individual callbacks in anonymous implementations.
 * 
 * @see ResourcesContainerListener
 */
public abstract class ResourceContainerAdapter<T> implements ResourcesContainerListener<T> {
  @Override
  public void added(String resourceName, T resource) {
  }

  @Override
  public void removed(String resourceName, T resource) {
  }

  @Override
  public void cleared() {
  }
}
