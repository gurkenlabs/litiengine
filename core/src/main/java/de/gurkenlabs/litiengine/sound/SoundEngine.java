package de.gurkenlabs.litiengine.sound;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ILaunchable;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.tweening.TweenFunction;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;

/**
 * This {@code SoundEngine} class provides all methods to play back sounds and music in your game. It allows to define
 * the 2D coordinates of the sound or even pass in the source entity of the sound which will adjust the position
 * according to the position of the entity.
 *
 * <p>
 * The sound engine supports .wav, .mp3 and .ogg by default. If you need other file extensions, you have to write an own
 * SPI implementation and inject it in your project.
 */
public final class SoundEngine implements IUpdateable, ILaunchable {
  public static final int DEFAULT_MAX_DISTANCE = 150;

  static final ExecutorService EXECUTOR =
      Executors.newCachedThreadPool(
          new ThreadFactory() {
            private int id = 0;

            @Override
            public Thread newThread(Runnable r) {
              return new Thread(r, "Sound Playback Thread " + ++id);
            }
          });

  private static final Logger log = Logger.getLogger(SoundEngine.class.getName());
  private Point2D listenerLocation;
  private UnaryOperator<Point2D> listenerLocationCallback = old -> Game.world().camera().getFocus();
  private int maxDist = DEFAULT_MAX_DISTANCE;
  private MusicPlayback music;
  private final Collection<MusicPlayback> allMusic = ConcurrentHashMap.newKeySet();
  private final Collection<SFXPlayback> sounds = ConcurrentHashMap.newKeySet();

  /**
   * <b>You should never call this manually! Instead use the {@code Game.audio()} instance.</b>
   *
   * @see Game#audio()
   */
  public SoundEngine() {
    if (Game.audio() != null) {
      throw new UnsupportedOperationException(
          "Never initialize a SoundEngine manually. Use Game.audio() instead.");
    }
  }

  /**
   * Gets the maximum distance from the listener at which a sound source can still be heard.
   *
   * @return The maximum distance at which a sound can be heard.
   */
  public int getMaxDistance() {
    return maxDist;
  }

  /**
   * Sets the currently playing track to a {@code LoopedTrack} with the sound defined by the specified music name. This
   * has no effect if the specified track is already playing.
   *
   * @param musicName
   *          The name of the {@code Sound} to be played.
   * @return The playback of the music
   */
  public MusicPlayback playMusic(String musicName) {
    return playMusic(Resources.sounds().get(musicName));
  }

  /**
   * Sets the currently playing track to a {@code LoopedTrack} with the specified music {@code
   * Sound}. This has no effect if the specified track is already playing.
   *
   * @param music
   *          The {@code Sound} to be played.
   * @return The playback of the music
   */
  public MusicPlayback playMusic(Sound music) {
    return playMusic(new LoopedTrack(music));
  }

  /**
   * Sets the currently playing track to the specified track. This has no effect if the specified track is already
   * playing.
   *
   * @param track
   *          The track to play
   * @return The playback of the music
   */
  public MusicPlayback playMusic(Track track) {
    return playMusic(track, null, false, true);
  }

  /**
   * Sets the currently playing track to the specified track.
   *
   * @param track
   *          The track to play
   * @param restart
   *          Whether to restart if the specified track is already playing, determined by {@link Object#equals(Object)}
   * @return The playback of the music
   */
  public MusicPlayback playMusic(Track track, boolean restart) {
    return playMusic(track, null, restart, true);
  }

  /**
   * Plays the specified track.
   *
   * @param track
   *          The track to play
   * @param restart
   *          Whether to restart if the specified track is already playing, determined by {@link Object#equals(Object)}
   * @param stop
   *          Whether to stop an existing track if present
   * @return The playback of the music
   */
  public MusicPlayback playMusic(Track track, boolean restart, boolean stop) {
    return playMusic(track, null, restart, stop);
  }

  /**
   * Plays the specified track, optionally configuring it before starting.
   *
   * @param track
   *          The track to play
   * @param config
   *          A call to configure the playback prior to starting, which can be {@code null}
   * @param restart
   *          Whether to restart if the specified track is already playing, determined by {@link Object#equals(Object)}
   * @param stop
   *          Whether to stop an existing track if present
   * @return The playback of the music
   */
  public synchronized MusicPlayback playMusic(
      Track track, Consumer<? super MusicPlayback> config, boolean restart, boolean stop) {
    if (!restart && music != null && music.isPlaying() && music.getTrack().equals(track)) {
      return music;
    }

    try (MusicPlayback playback = new MusicPlayback(track)) {
      if (config != null) {
        config.accept(playback);
      }
      if (stop) {
        stopMusic();
      }
      allMusic.add(playback);
      playback.start();
      music = playback;
      return playback;
    } catch (LineUnavailableException | IllegalArgumentException e) {
      resourceFailure(e);
      return null;
    }
  }

  /**
   * Fades out all music volume to 0 over the specified time.
   *
   * @param duration
   *          The fade duration in milliseconds
   */
  public void fadeMusic(int duration) {
    this.allMusic.forEach(m -> m.fade(duration));
  }

  /**
   * Fades all music volume to a given value over the specified time.
   *
   * @param duration
   *          The fade duration in milliseconds
   * @param target
   *          The target volume.
   * @param easeType
   *          The easing Function used for Tweening the volume
   */
  public void fadeMusic(int duration, float target, TweenFunction easeType) {
    this.allMusic.forEach(m -> m.fade(duration, target, easeType));
  }

  /**
   * Gets the "main" music that is playing. This usually means the last call to {@code playMusic}, though if the music has
   * been stopped it will be {@code null}.
   *
   * @return The main music, which could be {@code null}.
   */
  public synchronized MusicPlayback getMusic() {
    return music;
  }

  /**
   * Gets a list of all music playbacks.
   *
   * @return A list of all music playbacks.
   */
  public synchronized Collection<MusicPlayback> getAllMusic() {
    return Collections.unmodifiableCollection(allMusic);
  }

  /**
   * Plays the specified sound and updates its volume and pan by the current entity location in relation to the listener
   * location.
   *
   * @param entity
   *          The entity at which location the sound should be played.
   * @param sound
   *          The sound to play.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final Sound sound, final IEntity entity) {
    return playSound(sound, entity, false);
  }

  /**
   * Plays a {@code Sound} with the specified name and updates its volume and pan by the current entity location in
   * relation to the listener location.
   *
   * @param entity
   *          The entity at which location the sound should be played.
   * @param soundName
   *          The name of the sound to play.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final String soundName, final IEntity entity) {
    return playSound(Resources.sounds().get(soundName), entity, false);
  }

  /**
   * Plays the specified sound and updates its volume and pan by the current entity location in relation to the listener
   * location.
   *
   * @param entity
   *          The entity at which location the sound should be played.
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final Sound sound, final IEntity entity, boolean loop) {
    return playSound(sound, entity, loop, getMaxDistance(), 1f);
  }

  /**
   * Plays the specified sound and updates its volume and pan by the current entity location in relation to the listener
   * location.
   *
   * @param entity
   *          The entity at which location the sound should be played.
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @param range
   *          the range in pixels for which this sound can be heard
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final Sound sound, final IEntity entity, boolean loop, int range) {
    return playSound(sound, entity::getCenter, loop, range, 1f);
  }

  /**
   * Plays the specified sound and updates its volume and pan by the current entity location in relation to the listener
   * location.
   *
   * @param entity
   *          The entity at which location the sound should be played.
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @param range
   *          the range in pixels for which this sound can be heard
   * @param volume
   *          The volume modifier for the sound playback instance.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(
      final Sound sound, final IEntity entity, boolean loop, int range, float volume) {
    return playSound(sound, entity::getCenter, loop, range, volume);
  }

  /**
   * Plays a {@code Sound} with the specified name and updates its volume and pan by the current entity location in
   * relation to the listener location.
   *
   * @param entity
   *          The entity at which location the sound should be played.
   * @param soundName
   *          The name of the sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final String soundName, final IEntity entity, boolean loop) {
    return playSound(Resources.sounds().get(soundName), entity, loop);
  }

  /**
   * Plays the specified sound at the specified location and updates the volume and pan in relation to the listener
   * location.
   *
   * @param location
   *          The location at which to play the sound.
   * @param sound
   *          The sound to play.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final Sound sound, final Point2D location) {
    return playSound(sound, location, false);
  }

  /**
   * Plays a {@code Sound} with the specified name at the specified location and updates the volume and pan in relation to
   * the listener location.
   *
   * @param location
   *          The location at which to play the sound.
   * @param soundName
   *          The name of the sound to play.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final String soundName, final Point2D location) {
    return playSound(Resources.sounds().get(soundName), location, false);
  }

  /**
   * Plays the specified sound at the specified location and updates the volume and pan in relation to the listener
   * location.
   *
   * @param x
   *          The x-coordinate of the location at which to play the sound.
   * @param y
   *          The y-coordinate of the location at which to play the sound.
   * @param sound
   *          The sound to play.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final Sound sound, double x, double y) {
    return playSound(sound, new Point2D.Double(x, y), false);
  }

  /**
   * Plays a {@code Sound} with the specified name at the specified location and updates the volume and pan in relation to
   * the listener location.
   *
   * @param x
   *          The x-coordinate of the location at which to play the sound.
   * @param y
   *          The y-coordinate of the location at which to play the sound.
   * @param soundName
   *          The name of the sound to play.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final String soundName, double x, double y) {
    return playSound(Resources.sounds().get(soundName), new Point2D.Double(x, y), false);
  }

  /**
   * Plays the specified sound at the specified location and updates the volume and pan in relation to the listener
   * location.
   *
   * @param location
   *          The location at which to play the sound.
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final Sound sound, final Point2D location, boolean loop) {
    return playSound(sound, () -> location, loop, getMaxDistance(), 1f);
  }

  /**
   * Plays the specified sound at the specified location and updates the volume and pan in relation to the listener
   * location.
   *
   * @param location
   *          The location at which to play the sound.
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @param range
   *          the range in pixels for which this sound can be heard
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final Sound sound, final Point2D location, boolean loop, int range) {
    return playSound(sound, () -> location, loop, range, 1f);
  }

  /**
   * Plays the specified sound at the specified location and updates the volume and pan in relation to the listener
   * location.
   *
   * @param location
   *          The location at which to play the sound.
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @param range
   *          the range in pixels for which this sound can be heard
   * @param volume
   *          The volume modifier for the sound playback instance.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(
      final Sound sound, final Point2D location, boolean loop, int range, float volume) {
    return playSound(sound, () -> location, loop, range, volume);
  }

  /**
   * Plays a {@code Sound} with the specified name at the specified location and updates the volume and pan in relation to
   * the listener location.
   *
   * @param location
   *          The location at which to play the sound.
   * @param soundName
   *          The name of the sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final String soundName, final Point2D location, boolean loop) {
    return playSound(Resources.sounds().get(soundName), location, loop);
  }

  /**
   * Plays the specified sound at the specified location and updates the volume and pan in relation to the listener
   * location.
   *
   * @param x
   *          The x-coordinate of the location at which to play the sound.
   * @param y
   *          The y-coordinate of the location at which to play the sound.
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final Sound sound, final double x, final double y, boolean loop) {
    return playSound(sound, new Point2D.Double(x, y), loop);
  }

  /**
   * Plays a {@code Sound} with the specified name at the specified location and updates the volume and pan in relation to
   * the listener location.
   *
   * @param x
   *          The x-coordinate of the location at which to play the sound.
   * @param y
   *          The y-coordinate of the location at which to play the sound.
   * @param soundName
   *          The name of the sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(
      final String soundName, final double x, final double y, boolean loop) {
    return playSound(Resources.sounds().get(soundName), new Point2D.Double(x, y), loop);
  }

  /**
   * Plays the specified sound with the volume configured in the SOUND config with a center pan.
   *
   * @param sound
   *          The sound to play.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final Sound sound) {
    return playSound(sound, false);
  }

  /**
   * Plays a {@code Sound} with the specified name with the volume configured in the SOUND config with a center pan.
   *
   * @param soundName
   *          The name of the sound to play.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final String soundName) {
    return playSound(Resources.sounds().get(soundName), false);
  }

  /**
   * Plays the specified sound with the volume configured in the SOUND config with a center pan.
   *
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final Sound sound, boolean loop) {
    return playSound(sound, () -> null, loop, getMaxDistance(), 1f);
  }

  /**
   * Plays the specified sound with the volume configured in the SOUND config with a center pan.
   *
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @param range
   *          the range in pixels for which this sound can be heard
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final Sound sound, boolean loop, int range) {
    return playSound(sound, () -> null, loop, range, 1f);
  }

  /**
   * Plays the specified sound with the volume configured in the SOUND config with a center pan.
   *
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @param range
   *          the range in pixels for which this sound can be heard
   * @param volume
   *          The volume modifier for the sound playback instance.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final Sound sound, boolean loop, int range, float volume) {
    return playSound(sound, () -> null, loop, range, volume);
  }

  /**
   * Plays a {@code Sound} with the specified name with the volume configured in the SOUND config with a center pan.
   *
   * @param soundName
   *          The name of the sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @return An {@link SFXPlayback} instance that allows to further process and control the played sound.
   */
  public SFXPlayback playSound(final String soundName, boolean loop) {
    return playSound(Resources.sounds().get(soundName), loop);
  }

  /**
   * Sets the default maximum distance from the listener at which a sound source can still be heard. If the distance
   * between the sound source and the listener is greater than the specified value, the volume is set to 0.
   *
   * @param radius
   *          The maximum distance at which sounds can still be heard.
   */
  public void setMaxDistance(final int radius) {
    maxDist = radius;
  }

  /** Stops the playback of the current background music. */
  public synchronized void stopMusic() {
    for (MusicPlayback track : allMusic) {
      track.cancel();
    }
  }

  /**
   * Creates an {@code SFXPlayback} object that can be configured prior to starting. Also allows for a custom source
   * supplier.
   *
   * <p>
   * Unlike the {@code playSound} methods, the {@code SFXPlayback} objects returned by this method must be started using
   * the {@link SoundPlayback#start()} method. However, necessary resources are acquired <em>immediately</em> upon calling
   * this method, and will remain in use until the playback is either cancelled or finalized.
   *
   * @param sound
   *          The sound to play
   * @param supplier
   *          A function to get the sound's current source location (the sound is statically positioned if the location is
   *          {@code null})
   * @param loop
   *          Whether to loop the sound
   * @param range
   *          the range in pixels for which this sound can be heard
   * @param volume
   *          The volume modifier for the sound playback instance.
   * @return An {@code SFXPlayback} object that can be configured prior to starting, but will need to be manually started.
   */
  public SFXPlayback createSound(Sound sound, Supplier<Point2D> supplier, boolean loop, int range, float volume) {
    try (
        SFXPlayback playback = new SFXPlayback(sound, supplier, loop, range, volume);) {
      return playback;
    } catch (LineUnavailableException | IllegalArgumentException e) {
      resourceFailure(e);
      return null;
    }
  }

  /**
   * This method allows to set the callback that is used by the SoundEngine to determine where the listener location is.
   *
   * <p>
   * If not explicitly set, the SoundEngine uses the camera focus (center of the screen) as listener location.
   *
   * @param callback
   *          The callback that determines the location of the sound listener.
   */
  public void setListenerLocationCallback(UnaryOperator<Point2D> callback) {
    listenerLocationCallback = callback;
  }

  @Override
  public void start() {
    listenerLocation = Game.world().camera().getFocus();
  }

  @Override
  public void terminate() {
    if (music != null && music.isPlaying()) {
      music.cancel();
      music = null;
    }

    EXECUTOR.shutdown();
    synchronized (sounds) {
      for (SFXPlayback playback : sounds) {
        playback.cancel();
      }

      sounds.clear();
    }
  }

  @Override
  public void update() {
    listenerLocation = listenerLocationCallback.apply(listenerLocation);

    Iterator<SFXPlayback> iter = sounds.iterator();
    while (iter.hasNext()) {
      SFXPlayback s = iter.next();
      if (s.isPlaying()) {
        s.updateLocation(listenerLocation);
      } else {
        iter.remove();
      }
    }

    Iterator<MusicPlayback> iter2 = allMusic.iterator();
    while (iter.hasNext()) {
      MusicPlayback s = iter2.next();
      if (s.isPlaying()) {
        s.setMusicVolume(Game.config().sound().getMusicVolume());
      } else {
        iter.remove();
      }
    }

    if (music != null) {
      if (music.isPlaying()) {
        music.setMusicVolume(Game.config().sound().getMusicVolume());
      } else {
        music = null;
      }
    }
  }

  Point2D getListenerLocation() {
    return this.listenerLocation == null
        ? new Point2D.Double(0, 0)
        : (Point2D) this.listenerLocation.clone();
  }

  void addSound(SFXPlayback playback) {
    this.sounds.add(playback);
  }

  private SFXPlayback playSound(
      Sound sound, Supplier<Point2D> supplier, boolean loop, int range, float volume) {
    if (sound == null) {
      return null;
    }

    SFXPlayback playback = createSound(sound, supplier, loop, range, volume);
    if (playback == null) {
      return null;
    }
    playback.start();
    return playback;
  }

  private static void resourceFailure(Throwable e) {
    log.log(Level.WARNING, "could not open a line", e);
  }
}
