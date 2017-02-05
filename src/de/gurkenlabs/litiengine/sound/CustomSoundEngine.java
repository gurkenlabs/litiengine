package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;

public class CustomSoundEngine implements ISoundEngine, IUpdateable {
  private static final int DEFAULT_MAX_DISTANCE = 250;
  private final Map<String, SoundSource> sources;
  private final List<SoundSource> sounds;
  private SoundSource music;
  private Point2D listenerLocation;
  private float maxDist;

  public CustomSoundEngine() {
    this.sounds = new CopyOnWriteArrayList<>();
    this.sources = new ConcurrentHashMap<>();
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
  }

  @Override
  public void init(final float soundVolume) {

  }

  public boolean isPlaying(String identifier) {
    for (SoundSource s : this.sounds) {
      if (s.getSound().getName().equals(identifier)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void load(Sound sound) {
    if (sound == null || this.sources.containsKey(sound.getName())) {
      return;
    }

    SoundSource soundSource = new SoundSource(sound);
    this.sources.put(sound.getName(), soundSource);
  }

  @Override
  public void playMusic(Sound s) {
    if (this.music != null) {
      this.music.dispose();
    }

    this.music = new SoundSource(s);
    this.music.play(true);
  }

  @Override
  public void playSound(IEntity entity, Sound s) {
    if (s == null) {
      return;
    }

    SoundSource source = this.getSoundSource(s, entity);
    source.play();
    this.sounds.add(source);
  }

  @Override
  public void playSound(Point2D location, Sound s) {
    if (s == null) {
      return;
    }

    SoundSource source = this.getSoundSource(s, location);
    source.play();
    this.sounds.add(source);
  }

  @Override
  public void playSound(Sound s) {
    if (s == null) {
      return;
    }

    SoundSource source = this.getSoundSource(s);
    source.play();
    this.sounds.add(source);
  }

  @Override
  public void rewind(Sound s) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setMaxDistance(float radius) {
    this.maxDist = radius;
  }

  @Override
  public void stopMusic(Sound s) {
    this.music.dispose();
    this.music = null;
  }

  @Override
  public float getMaxDistance() {
    return this.maxDist;
  }

  private SoundSource getSoundSource(Sound sound, IEntity entity) {
    if (this.sources.containsKey(sound.getName())) {
      boolean currentlyPlaying = this.sounds.contains(this.sources.get(sound.getName()));

      if (!currentlyPlaying) {
        return this.sources.get(sound.getName());
      }
    }

    SoundSource s = new SoundSource(sound, this.listenerLocation, entity);
    return s;
  }

  private SoundSource getSoundSource(Sound sound, Point2D location) {
    if (this.sources.containsKey(sound.getName())) {
      boolean currentlyPlaying = this.sounds.contains(this.sources.get(sound.getName()));

      if (!currentlyPlaying) {
        return this.sources.get(sound.getName());
      }
    }

    SoundSource s = new SoundSource(sound, this.listenerLocation, location);
    return s;
  }

  private SoundSource getSoundSource(Sound sound) {
    if (this.sources.containsKey(sound.getName())) {
      boolean currentlyPlaying = this.sounds.contains(this.sources.get(sound.getName()));

      if (!currentlyPlaying) {
        return this.sources.get(sound.getName());
      }
    }

    SoundSource s = new SoundSource(sound, this.listenerLocation);
    return s;
  }
}
