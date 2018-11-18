package de.gurkenlabs.litiengine;

/**
 * An abstract implementation of a <code>GameListener</code> that allows to only overwrite
 * individual callbacks in anonymous implementations.
 * 
 * @see GameListener
 */
public abstract class GameAdapter implements GameListener {

  @Override
  public void started() {
  }

  @Override
  public boolean terminating() {
    return true;
  }

  @Override
  public void terminated() {
  }

  @Override
  public void initialized(String... args) {
  }
}
