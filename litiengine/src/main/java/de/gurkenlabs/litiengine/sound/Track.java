package de.gurkenlabs.litiengine.sound;

import javax.sound.sampled.AudioFormat;

/// The `Track` class defines a sequence in which music `Sounds` should be played back by the engine.
///
/// This is useful to further define how music is played in the engine.
///
/// @see SoundEngine#playMusic(Track)
public interface Track extends Iterable<Sound> {
  AudioFormat getFormat();
}
