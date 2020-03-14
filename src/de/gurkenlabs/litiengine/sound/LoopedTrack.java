package de.gurkenlabs.litiengine.sound;

import java.util.Iterator;
import java.util.Objects;

import javax.sound.sampled.AudioFormat;

import de.gurkenlabs.litiengine.resources.Resources;

public class LoopedTrack implements Track, Iterator<Sound> {
  private Sound track;

  /**
   * Initializes a new <code>LoopedTrack</code> for the specified sound.
   * 
   * @param soundName
   *          The name of the sound to be played by this track.
   */
  public LoopedTrack(String soundName) {
    this(Resources.sounds().get(soundName));
  }

  /**
   * Initializes a new <code>LoopedTrack</code> for the specified sound.
   * 
   * @param sound
   *          The sound to be played by this track.
   */
  public LoopedTrack(Sound sound) {
    this.track = Objects.requireNonNull(sound);
  }

  @Override
  public Iterator<Sound> iterator() {
    return this;
  }

  @Override
  public AudioFormat getFormat() {
    return this.track.getFormat();
  }

  // implement the iterator here to avoid allocating new objects
  // they don't have any state data anyway
  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public Sound next() {
    return this.track;
  }

  @Override
  public boolean equals(Object anObject) {
    return this == anObject || anObject instanceof LoopedTrack && ((LoopedTrack) anObject).track.equals(this.track);
  }

  @Override
  public int hashCode() {
    return this.track.hashCode();
  }

  @Override
  public String toString() {
    return "looped track: " + this.track;
  }
}
