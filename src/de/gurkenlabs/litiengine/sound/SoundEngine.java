package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;

public class SoundEngine implements ISoundEngine, IUpdateable {
  private static final int DEFAULT_MAX_DISTANCE = 250;
  private final List<SoundSource> sounds;
  private SoundSource music;
  private Point2D listenerLocation;
  private float maxDist;

  public SoundEngine() {
    this.sounds = new CopyOnWriteArrayList<>();
    this.maxDist = DEFAULT_MAX_DISTANCE;
  }

  @Override
  public void start() {
    Game.getLoop().registerForUpdate(this);
    this.listenerLocation = Game.getScreenManager().getCamera().getFocus();
  }

  @Override
  public void terminate() {
    Game.getLoop().unregisterFromUpdate(this);
    SoundSource.terminate();
  }

  @Override
  public void update(IGameLoop loop) {
    this.listenerLocation = Game.getScreenManager().getCamera().getFocus();

    List<SoundSource> remove = new ArrayList<>();
    for (SoundSource s : this.sounds) {
      if (s != null && !s.isPlaying()) {
        s.dispose();
        remove.add(s);
      }
    }

    this.sounds.removeAll(remove);
    for (SoundSource s : this.sounds) {
      s.updateControls(this.listenerLocation);
    }

    // music is looped by default
    if (this.music != null && !this.music.isPlaying()) {
      this.playMusic(this.music.getSound());
    }
  }

  @Override
  public void playMusic(Sound sound) {
    if (this.music != null) {
      this.music.dispose();
    }

    this.music = new SoundSource(sound);
    this.music.play(true, Game.getConfiguration().SOUND.getMusicVolume());
  }

  @Override
  public void playSound(IEntity entity, Sound sound) {
    if (sound == null) {
      return;
    }

    SoundSource source = new SoundSource(sound, this.listenerLocation, entity);
    source.play();
    this.sounds.add(source);
  }

  @Override
  public void playSound(Point2D location, Sound sound) {
    if (sound == null) {
      return;
    }

    SoundSource source = new SoundSource(sound, this.listenerLocation);
    source.play();
    this.sounds.add(source);
  }

  @Override
  public void playSound(Sound sound) {
    if (sound == null) {
      return;
    }

    SoundSource source = new SoundSource(sound);
    source.play();
    this.sounds.add(source);
  }

  @Override
  public void setMaxDistance(float radius) {
    this.maxDist = radius;
  }

  @Override
  public void stopMusic() {
    this.music.dispose();
    this.music = null;
  }

  @Override
  public float getMaxDistance() {
    return this.maxDist;
  }
}
