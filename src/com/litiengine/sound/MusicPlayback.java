package com.litiengine.sound;

import javax.sound.sampled.LineUnavailableException;

import com.litiengine.Game;

/**
 * A {@code SoundPlayback} implementation for the playback music.
 */
public class MusicPlayback extends SoundPlayback {
  private Track track;
  private VolumeControl musicVolume;

  MusicPlayback(Track track) throws LineUnavailableException {
    super(track.getFormat());
    this.track = track;
    this.musicVolume = this.createVolumeControl();
    this.musicVolume.set(Game.config().sound().getMusicVolume());
  }

  @Override
  public void run() {
    for (Sound sound : this.track) {
      if (this.play(sound)) {
        return;
      }
    }
    this.finish();
  }

  public Track getTrack() {
    return this.track;
  }

  void setMusicVolume(float volume) {
    this.musicVolume.set(volume);
  }
}
