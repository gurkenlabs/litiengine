package de.gurkenlabs.litiengine.sound;

import javax.sound.sampled.AudioFormat;

/**
 * The {@code Track} class defines a sequence in which music {@code Sounds} should be played back by the engine.
 * <p>
 * This is useful to further define how music is played in the engine.
 * </p>
 * 
 * @see SoundEngine#playMusic(Track)
 */
public interface Track extends Iterable<Sound> {
  public AudioFormat getFormat();
}
