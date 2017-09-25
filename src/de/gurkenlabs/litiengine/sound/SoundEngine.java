package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;

public final class SoundEngine implements ISoundEngine, IUpdateable {
  private static final int DEFAULT_MAX_DISTANCE = 250;
  private Point2D listenerLocation;
  private Function<Point2D, Point2D> listenerLocationCallback;
  private float maxDist;
  private SoundSource music;
  private final List<SoundSource> sounds;

  public SoundEngine() {
    this.sounds = new CopyOnWriteArrayList<>();
    this.maxDist = DEFAULT_MAX_DISTANCE;
    this.setListenerLocationCallback(old -> Game.getCamera().getFocus());
  }

  @Override
  public float getMaxDistance() {
    return this.maxDist;
  }

  @Override
  public void playMusic(final Sound sound) {
    if (sound == null) {
      return;
    }

    if (this.music != null) {
      this.music.dispose();
    }

    this.music = new SoundSource(sound);
    this.music.play(true, Game.getConfiguration().sound().getMusicVolume());
  }

  @Override
  public void playSound(final IEntity entity, final Sound sound) {
    if (sound == null) {
      return;
    }

    final SoundSource source = new SoundSource(sound, this.listenerLocation, entity);
    source.play();
    this.sounds.add(source);
  }

  @Override
  public void playSound(final Point2D location, final Sound sound) {
    if (sound == null) {
      return;
    }

    final SoundSource source = new SoundSource(sound, this.listenerLocation);
    source.play();
    this.sounds.add(source);
  }

  @Override
  public void playSound(final Sound sound) {
    if (sound == null) {
      return;
    }

    final SoundSource source = new SoundSource(sound);
    source.play();
    this.sounds.add(source);
  }

  @Override
  public void setMaxDistance(final float radius) {
    this.maxDist = radius;
  }

  @Override
  public void start() {
    Game.getLoop().attach(this);
    this.listenerLocation = Game.getCamera().getFocus();
  }

  @Override
  public void stopMusic() {
    if (music == null) {
      return;
    }

    this.music.dispose();
    this.music = null;
  }

  @Override
  public void terminate() {
    Game.getLoop().detach(this);
    SoundSource.terminate();
  }

  @Override
  public void update(final IGameLoop loop) {
    this.listenerLocation = this.listenerLocationCallback.apply(this.listenerLocation);

    final List<SoundSource> remove = new ArrayList<>();
    for (final SoundSource s : this.sounds) {
      if (s != null && !s.isPlaying()) {
        s.dispose();
        remove.add(s);
      }
    }

    this.sounds.removeAll(remove);
    for (final SoundSource s : this.sounds) {
      s.updateControls(this.listenerLocation);
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
