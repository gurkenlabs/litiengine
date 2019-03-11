package de.gurkenlabs.litiengine.sound;

import javax.sound.sampled.AudioFormat;

public interface Track extends Iterable<Sound> {
  public AudioFormat getFormat();
}
