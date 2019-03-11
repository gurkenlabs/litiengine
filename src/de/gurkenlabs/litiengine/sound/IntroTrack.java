package de.gurkenlabs.litiengine.sound;

import java.util.Iterator;
import java.util.Objects;

import javax.sound.sampled.AudioFormat;

public class IntroTrack implements Track {
  private Sound intro;
  private Sound loop;

  public IntroTrack(Sound intro, Sound loop) {
    if (!intro.getFormat().equals(loop.getFormat())) {
      throw new IllegalArgumentException(intro.getFormat() + " does not match " + loop.getFormat());
    }
    Objects.requireNonNull(intro);
    Objects.requireNonNull(loop);
    this.intro = intro;
    this.loop = loop;
  }

  private class Iter implements Iterator<Sound> {
    private boolean first = true;

    @Override
    public boolean hasNext() {
      return true;
    }

    @Override
    public Sound next() {
      if (this.first) {
        this.first = false;
        return IntroTrack.this.intro;
      }
      return IntroTrack.this.loop;
    }
  }

  @Override
  public Iterator<Sound> iterator() {
    return new Iter();
  }

  @Override
  public AudioFormat getFormat() {
    return this.loop.getFormat();
  }

  public Sound getIntro() {
    return this.intro;
  }

  public Sound getLoop() {
    return this.loop;
  }

  @Override
  public boolean equals(Object anObject) {
    if (this == anObject) {
      return true;
    }
    if (anObject == null || !(anObject instanceof IntroTrack)) {
      return false;
    }
    IntroTrack other = (IntroTrack) anObject;
    return this.intro == other.intro && this.loop == other.loop;
  }

  @Override
  public int hashCode() {
    return this.loop.hashCode() * 31 + this.intro.hashCode();
  }

  @Override
  public String toString() {
    return "looped track: " + this.loop + ", with intro: " + this.intro;
  }
}
