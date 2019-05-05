package de.gurkenlabs.litiengine.sound;

import java.util.Iterator;
import java.util.Objects;

import javax.sound.sampled.AudioFormat;

import de.gurkenlabs.litiengine.resources.Resources;

public class LoopedTrack implements Track, Iterator<Sound> {
  private Sound track;

  public LoopedTrack(String track) {
    this(Resources.sounds().get(track));
  }

  public LoopedTrack(Sound track) {
    Objects.requireNonNull(track);
    this.track = track;
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
    return this == anObject || anObject != null && anObject instanceof LoopedTrack && ((LoopedTrack) anObject).track.equals(this.track);
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
