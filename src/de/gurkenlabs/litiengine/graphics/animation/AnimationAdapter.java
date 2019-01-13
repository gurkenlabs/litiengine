package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.GameListener;

/**
 * An abstract implementation of a <code>AnimationListener</code> that allows to only overwrite
 * individual callbacks in anonymous implementations.
 * 
 * @see GameListener
 */
public abstract class AnimationAdapter implements AnimationListener {

  @Override
  public void played(Animation animation) {
  }

  @Override
  public void finished(Animation animation) {
  }
}
