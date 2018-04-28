package de.gurkenlabs.litiengine.sound;

public interface ISoundPlayback {
  /**
   * Adds a <code>SoundPlaybackListener</code> to this instance.
   *
   * @param listener
   *          The <code>SoundPlaybackListener</code> to be added.
   */
  public void addSoundPlaybackListener(SoundPlaybackListener listener);

  /**
   * Removes a <code>SoundPlaybackListener</code> to this instance.
   *
   * @param listener
   *          The <code>SoundPlaybackListener</code> to be removed.
   */
  public void removeSoundPlaybackListener(SoundPlaybackListener listener);

  public void pausePlayback();

  public void resumePlayback();

  public boolean isPaused();

  public boolean isPlaying();

  public void cancel();

  public float getGain();

  public void setGain(float gain);
}
