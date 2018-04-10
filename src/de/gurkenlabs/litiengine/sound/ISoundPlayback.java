package de.gurkenlabs.litiengine.sound;

import java.util.function.Consumer;

public interface ISoundPlayback {
  public void pausePlayback();

  public void resumePlayback();

  public boolean isPaused();

  public boolean isPlaying();

  public void cancel();

  public void onFinished(Consumer<Sound> consumer);

  public void onCancelled(Consumer<Sound> consumer);
}
