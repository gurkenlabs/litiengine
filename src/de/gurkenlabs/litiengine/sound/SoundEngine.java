package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ILaunchable;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.input.Input;

public final class SoundEngine implements ISoundEngine, IUpdateable, ILaunchable {
  public static final int DEFAULT_MAX_DISTANCE = 150;
  private Point2D listenerLocation;
  private Function<Point2D, Point2D> listenerLocationCallback;
  private float maxDist;
  private SoundPlayback music;
  private final List<SoundPlayback> sounds;

  public SoundEngine() {
    this.sounds = Collections.synchronizedList(new ArrayList<>());
    this.maxDist = DEFAULT_MAX_DISTANCE;
    this.setListenerLocationCallback(old -> Game.world().camera().getFocus());
  }

  @Override
  public float getMaxDistance() {
    return this.maxDist;
  }

  @Override
  public ISoundPlayback playMusic(final Sound sound) {
    return this.playMusic(sound, true);
  }

  @Override
  public ISoundPlayback playMusic(final Sound sound, boolean loop) {
    if (sound == null || this.music != null && sound.equals(this.music.getSound())) {
      return null;
    }

    if (this.music != null) {
      this.music.cancel();
    }

    this.music = new SoundPlayback(sound);
    this.music.play(loop, Game.config().sound().getMusicVolume());
    return this.music;
  }

  @Override
  public ISoundPlayback playSound(final IEntity entity, final Sound sound) {
    return playSound(entity, sound, false);
  }

  @Override
  public ISoundPlayback playSound(final IEntity entity, final Sound sound, boolean loop) {
    if (sound == null) {
      return null;
    }

    final SoundPlayback playback = new SoundPlayback(sound, this.listenerLocation, entity);
    playback.play(loop);
    this.sounds.add(playback);
    return playback;
  }

  @Override
  public ISoundPlayback playSound(final Point2D location, final Sound sound) {
    return this.playSound(location, sound, false);
  }

  @Override
  public ISoundPlayback playSound(final Point2D location, final Sound sound, boolean loop) {
    if (sound == null) {
      return null;
    }

    final SoundPlayback playback = new SoundPlayback(sound, this.listenerLocation);
    playback.play(loop);
    this.sounds.add(playback);
    return playback;
  }

  @Override
  public ISoundPlayback playSound(final Sound sound) {
    return playSound(sound, false);
  }

  @Override
  public ISoundPlayback playSound(final Sound sound, boolean loop) {
    if (sound == null) {
      return null;
    }

    final SoundPlayback playback = new SoundPlayback(sound);
    playback.play(loop);
    this.sounds.add(playback);

    return playback;
  }

  @Override
  public void setMaxDistance(final float radius) {
    this.maxDist = radius;
  }

  @Override
  public void start() {
    Input.getLoop().attach(this);
    this.listenerLocation = Game.world().camera().getFocus();
  }

  @Override
  public void stopMusic() {
    if (music == null) {
      return;
    }

    this.music.cancel();
    this.music = null;
  }

  @Override
  public void terminate() {
    Input.getLoop().detach(this);
    if (this.music != null && this.music.isPlaying()) {
      this.music.cancel();
      this.music = null;
    }

    synchronized (this.sounds) {
      for (SoundPlayback playback : this.sounds) {
        playback.cancel();
      }
  
      this.sounds.clear();
    }
    SoundPlayback.terminate();
  }

  @Override
  public void update() {
    this.listenerLocation = this.listenerLocationCallback.apply(this.listenerLocation);

    synchronized (this.sounds) {
      Iterator<SoundPlayback> iter = this.sounds.iterator();
      while (iter.hasNext()) {
        SoundPlayback s = iter.next();
        if (s != null) {
          if (!s.isPlaying()) {
            iter.remove();
          } else {
            s.updateControls(this.listenerLocation);
          }
        }
      }
    }

    if (this.music != null) {
      this.music.setMasterGain(Game.config().sound().getMusicVolume());
    }

    // music is looped by default
    if (this.music != null && !this.music.isPlaying()) {
      this.playMusic(this.music.getSound());
    }
  }

  @Override
  public void setListenerLocationCallback(Function<Point2D, Point2D> listenerLocationCallback) {
    this.listenerLocationCallback = listenerLocationCallback;
  }
}
