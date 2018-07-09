package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ILaunchable;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.input.Input;

/**
 * This class provides all methods to playback sounds and music in your
 * game. It allows to define the 2D coordinates of the sound or even pass in the
 * source entity of the sound which will adjust the position accoring to the
 * position of the entity. The LILI Engine Sound Engine supports .wav, .mp3 and
 * .ogg by default. If you need other file extensions, you have to write an own
 * SPI implementation and inject it in your project.
 */
public final class SoundEngine implements ILaunchable, IUpdateable {
  private static final int DEFAULT_MAX_DISTANCE = 150;
  private Point2D listenerLocation;
  private Function<Point2D, Point2D> listenerLocationCallback;
  private float maxDist;
  private SoundPlayback music;
  private final List<SoundPlayback> sounds;

  public SoundEngine() {
    this.sounds = new CopyOnWriteArrayList<>();
    this.maxDist = DEFAULT_MAX_DISTANCE;
    this.setListenerLocationCallback(old -> Game.getCamera().getFocus());
  }

  /**
   * Gets the maximum distance from the listener at which a sound source can
   * still be heard.
   * 
   * @return The maximum distance at which a sound can be heard.
   */
  public float getMaxDistance() {
    return this.maxDist;
  }

  /**
   * Loops the specified sound file as background music. If another music was
   * specified beforehand, its play-back will get interrupted and the new one
   * will be played.
   * 
   * @param sound
   *          The sound from which to play the background music.
   * @return A {@link SoundPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public SoundPlayback playMusic(final Sound sound) {
    if (sound == null || this.music != null && sound.equals(this.music.getSound())) {
      return null;
    }

    if (this.music != null) {
      this.music.dispose();
    }

    this.music = new SoundPlayback(sound);
    this.music.play(true, Game.getConfiguration().sound().getMusicVolume());
    return this.music;
  }

  /**
   * Plays the specified sound and updates its volume and pan by the current
   * entity location in relation to the listener location.
   * 
   * @param entity
   *          The entity at which location the sound should be played.
   * @param sound
   *          The sound to play.
   * 
   * @return A {@link SoundPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public SoundPlayback playSound(final IEntity entity, final Sound sound) {
    if (sound == null) {
      return null;
    }

    final SoundPlayback playback = new SoundPlayback(sound, this.listenerLocation, entity);
    playback.play();
    this.sounds.add(playback);
    return playback;
  }

  /**
   * Plays the specified sound at the specified location and updates the volume
   * and pan in relation to the listener location.
   * 
   * @param location
   *          The location at which to play the sound.
   * @param sound
   *          The sound to play.
   * 
   * @return A {@link SoundPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public SoundPlayback playSound(final Point2D location, final Sound sound) {
    if (sound == null) {
      return null;
    }

    final SoundPlayback playback = new SoundPlayback(sound, this.listenerLocation);
    playback.play();
    this.sounds.add(playback);
    return playback;
  }

  /**
   * Plays the specified sound with the volume configured in the SOUND config
   * with a center pan.
   * 
   * @param sound
   *          The sound to play.
   * 
   * @return A {@link SoundPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public SoundPlayback playSound(final Sound sound) {
    if (sound == null) {
      return null;
    }

    final SoundPlayback playback = new SoundPlayback(sound);
    playback.play();
    this.sounds.add(playback);

    return playback;
  }

  /**
   * Sets the maximum distance from the listener at which a sound source can
   * still be heard. If the distance between the sound source and the listener
   * is greater than the specified value, the volume is set to 0.
   * 
   * @param radius
   *          The maximum distance at which sounds can still be heard.
   */
  public void setMaxDistance(final float radius) {
    this.maxDist = radius;
  }

  @Override
  public void start() {
    Input.getLoop().attach(this);
    this.listenerLocation = Game.getCamera().getFocus();
  }

  /**
   * Stops the playback of the current background music.
   */
  public void stopMusic() {
    if (music == null) {
      return;
    }

    this.music.dispose();
    this.music = null;
  }

  @Override
  public void terminate() {
    Input.getLoop().detach(this);
    if (this.music != null && this.music.isPlaying()) {
      this.music.dispose();
      this.music = null;
    }

    for (SoundPlayback playback : this.sounds) {
      playback.dispose();
    }

    this.sounds.clear();
    SoundPlayback.terminate();
  }

  @Override
  public void update() {
    this.listenerLocation = this.listenerLocationCallback.apply(this.listenerLocation);

    final List<SoundPlayback> remove = new ArrayList<>();
    for (final SoundPlayback s : this.sounds) {
      if (s != null && !s.isPlaying()) {
        s.dispose();
        remove.add(s);
      }
    }

    this.sounds.removeAll(remove);
    for (final SoundPlayback s : this.sounds) {
      s.updateControls(this.listenerLocation);
    }

    if (this.music != null) {
      this.music.setMasterGain(Game.getConfiguration().sound().getMusicVolume());
    }

    // music is looped by default
    if (this.music != null && !this.music.isPlaying()) {
      this.playMusic(this.music.getSound());
    }
  }

  /**
   * This method allows to set the callback that is used by the SoundEngine to
   * determine where the listener location is.
   * 
   * If not explicitly set, the SoundEngine uses the camera focus (center of the
   * screen) as listener location.
   * 
   * @param listenerLocationCallback
   *          The callback that determines the location of the sound listener.
   */
  public void setListenerLocationCallback(Function<Point2D, Point2D> listenerLocationCallback) {
    this.listenerLocationCallback = listenerLocationCallback;
  }
}
