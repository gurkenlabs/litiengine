package de.gurkenlabs.litiengine.sound;

/**
 * An abstract implementation of a <code>SoundPlaybackListener</code> that allows to only overwrite
 * individual callbacks in anonymous implementations.
 * 
 * @see SoundPlaybackListener
 */
public abstract class SoundPlaybackAdapter implements SoundPlaybackListener {
  @Override
  public void cancelled(SoundEvent event) {
  }

  @Override
  public void finished(SoundEvent event) {
  }
}
