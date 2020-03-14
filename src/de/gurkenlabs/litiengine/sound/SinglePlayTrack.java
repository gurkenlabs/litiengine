package de.gurkenlabs.litiengine.sound;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.sound.sampled.AudioFormat;

import de.gurkenlabs.litiengine.resources.Resources;

/**
 * A <code>Track</code> that plays a sound once and then stops.
 */
public class SinglePlayTrack implements Track {
  private Sound sound;

  private class Iter implements Iterator<Sound> {
    private boolean hasNext = true;

    @Override
    public boolean hasNext() {
      return this.hasNext;
    }

    @Override
    public Sound next() {
      if (!this.hasNext) {
        throw new NoSuchElementException();
      }
      this.hasNext = false;
      return SinglePlayTrack.this.sound;
    }
  }

  /**
   * Initializes a new <code>SinglePlayTrack</code> for the specified sound.
   * 
   * @param soundName
   *          The name of the sound to be played by this track.
   */
  public SinglePlayTrack(String soundName) {
    this(Resources.sounds().get(soundName));
  }

  /**
   * Initializes a new <code>SinglePlayTrack</code> for the specified sound.
   * 
   * @param sound
   *          The sound to be played by this track.
   */
  public SinglePlayTrack(Sound sound) {
    this.sound = sound;
  }

  @Override
  public Iterator<Sound> iterator() {
    return new Iter();
  }

  @Override
  public AudioFormat getFormat() {
    return this.sound.getFormat();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof SinglePlayTrack && this.sound == ((SinglePlayTrack) obj).sound;
  }

  @Override
  public int hashCode() {
    // add a constant to avoid collisions with LoopedTrack
    return this.sound.hashCode() + 0xdb9857d0;
  }

  @Override
  public String toString() {
    return "track: " + this.sound.getName() + " (not looped)";
  }
}
