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
import de.gurkenlabs.litiengine.resources.Resources;

/**
 * This class provides all methods to playback sounds and music in your
 * game. It allows to define the 2D coordinates of the sound or even pass in the
 * source entity of the sound which will adjust the position according to the
 * position of the entity. The LILIengine sound engine supports .wav, .mp3 and
 * .ogg by default. If you need other file extensions, you have to write an own
 * SPI implementation and inject it in your project.
 * 
 * @see Game#audio()
 */
public final class SoundEngine implements IUpdateable, ILaunchable {
  public static final int DEFAULT_MAX_DISTANCE = 150;
  private Point2D listenerLocation;
  private Function<Point2D, Point2D> listenerLocationCallback;
  private float maxDist;
  private SoundPlayback music;
  private final List<SoundPlayback> sounds;

  /**
   * Instantiates a new SoundEngine instance.
   * 
   * <p>
   * <b>You should never call this manually! Instead use the <code>Game.audio()</code> instance.</b>
   * </p>
   * 
   * @see Game#audio()
   */
  public SoundEngine() {
    this.sounds = Collections.synchronizedList(new ArrayList<>());
    this.maxDist = DEFAULT_MAX_DISTANCE;
    this.setListenerLocationCallback(old -> Game.world().camera().getFocus());
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
   * @return A {@link ISoundPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public ISoundPlayback playMusic(final Sound sound) {
    return this.playMusic(sound, true);
  }

  public ISoundPlayback playMusic(final String sound) {
    return this.playMusic(Resources.sounds().get(sound), true);
  }

  /**
   * Plays the specified sound file as background music. If another music was
   * specified beforehand, its play-back will get interrupted and the new one
   * will be played.
   * 
   * @param sound
   *          The sound from which to play the background music.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @return A {@link ISoundPlayback} instance that allows to further process
   *         and control the played sound.
   */
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

  public ISoundPlayback playMusic(final String sound, boolean loop) {
    return this.playMusic(Resources.sounds().get(sound), loop);
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
   * @return A {@link ISoundPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public ISoundPlayback playSound(final Sound sound, final IEntity entity) {
    return playSound(sound, entity, false);
  }

  public ISoundPlayback playSound(final String sound, final IEntity entity) {
    return playSound(Resources.sounds().get(sound), entity, false);
  }

  /**
   * Plays the specified sound and updates its volume and pan by the current
   * entity location in relation to the listener location.
   * 
   * @param entity
   *          The entity at which location the sound should be played.
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @return A {@link ISoundPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public ISoundPlayback playSound(final Sound sound, final IEntity entity, boolean loop) {
    if (sound == null) {
      return null;
    }

    final SoundPlayback playback = new SoundPlayback(sound, this.listenerLocation, entity);
    playback.play(loop);
    this.sounds.add(playback);
    return playback;
  }

  public ISoundPlayback playSound(final String sound, final IEntity entity, boolean loop) {
    return this.playSound(Resources.sounds().get(sound), entity, loop);
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
   * @return A {@link ISoundPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public ISoundPlayback playSound(final Sound sound, final Point2D location) {
    return this.playSound(sound, location, false);
  }

  public ISoundPlayback playSound(final String sound, final Point2D location) {
    return this.playSound(Resources.sounds().get(sound), location, false);
  }

  public ISoundPlayback playSound(final Sound sound, double x, double y) {
    return this.playSound(sound, x, y, false);
  }

  public ISoundPlayback playSound(final String sound, double x, double y) {
    return this.playSound(Resources.sounds().get(sound), x, y, false);
  }

  /**
   * Plays the specified sound at the specified location and updates the volume
   * and pan in relation to the listener location.
   * 
   * @param location
   *          The location at which to play the sound.
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @return A {@link ISoundPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public ISoundPlayback playSound(final Sound sound, final Point2D location, boolean loop) {
    if (sound == null) {
      return null;
    }

    final SoundPlayback playback = new SoundPlayback(sound, this.listenerLocation, location);
    playback.play(loop);
    this.sounds.add(playback);
    return playback;
  }

  public ISoundPlayback playSound(final String sound, final Point2D location, boolean loop) {
    return this.playSound(Resources.sounds().get(sound), location, loop);
  }

  public ISoundPlayback playSound(final Sound sound, final double x, final double y, boolean loop) {
    return this.playSound(sound, new Point2D.Double(x, y), loop);
  }

  public ISoundPlayback playSound(final String sound, final double x, final double y, boolean loop) {
    return this.playSound(Resources.sounds().get(sound), x, y, loop);
  }

  /**
   * Plays the specified sound with the volume configured in the SOUND config
   * with a center pan.
   * 
   * @param sound
   *          The sound to play.
   * 
   * @return A {@link ISoundPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public ISoundPlayback playSound(final Sound sound) {
    return playSound(sound, false);
  }

  public ISoundPlayback playSound(final String sound) {
    return playSound(Resources.sounds().get(sound));
  }

  /**
   * Plays the specified sound with the volume configured in the SOUND config
   * with a center pan.
   * 
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @return A {@link ISoundPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public ISoundPlayback playSound(final Sound sound, boolean loop) {
    if (sound == null) {
      return null;
    }

    final SoundPlayback playback = new SoundPlayback(sound);
    playback.play(loop);
    this.sounds.add(playback);

    return playback;
  }

  public ISoundPlayback playSound(final String sound, boolean loop) {
    return this.playSound(Resources.sounds().get(sound), loop);
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

  /**
   * Stops the playback of the current background music.
   */
  public void stopMusic() {
    if (music == null) {
      return;
    }

    this.music.cancel();
    this.music = null;
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

  @Override
  public void start() {
    Game.inputLoop().attach(this);
    this.listenerLocation = Game.world().camera().getFocus();
  }

  @Override
  public void terminate() {
    Game.inputLoop().detach(this);
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
}
