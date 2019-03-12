package de.gurkenlabs.litiengine.sound;

import java.util.Iterator;
import java.util.Objects;

import javax.sound.sampled.AudioFormat;

import de.gurkenlabs.litiengine.resources.Resources;

public class IntroTrack implements Track {
  private Sound intro;
  private Sound loop;

  public IntroTrack(String intro, String loop) {
    this(Resources.sounds().get(intro), Resources.sounds().get(loop));
  }

  public IntroTrack(Sound intro, String loop) {
    this(intro, Resources.sounds().get(loop));
  }

  public IntroTrack(String intro, Sound loop) {
    this(Resources.sounds().get(intro), loop);
  }

  public IntroTrack(Sound intro, Sound loop) {
    Objects.requireNonNull(intro);
    Objects.requireNonNull(loop);
    if (!intro.getFormat().matches(loop.getFormat())) {
      throw new IllegalArgumentException(intro.getFormat() + " does not match " + loop.getFormat());
    }
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
    if (!(anObject instanceof IntroTrack)) {
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
