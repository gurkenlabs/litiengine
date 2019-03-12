package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.LineUnavailableException;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ILaunchable;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.SoundPlayback.VolumeControl;

/**
 * This class provides all methods to playback sounds and music in your
 * game. It allows to define the 2D coordinates of the sound or even pass in the
 * source entity of the sound which will adjust the position according to the
 * position of the entity. The LILIengine sound engine supports .wav, .mp3 and
 * .ogg by default. If you need other file extensions, you have to write an own
 * SPI implementation and inject it in your project.
 */
public final class SoundEngine implements IUpdateable, ILaunchable {
  private static final Logger log = Logger.getLogger(SoundEngine.class.getName());
  public static final int DEFAULT_MAX_DISTANCE = 150;
  static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactory() {
    private int id = 0;

    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, "Sound Playback Thread " + ++id);
    }
  });
  private static Point2D listenerLocation;
  private static Function<Point2D, Point2D> listenerLocationCallback = old -> Game.world().camera().getFocus();
  private static float maxDist = DEFAULT_MAX_DISTANCE;
  private static MusicPlayback music;
  private static Collection<MusicPlayback> allMusic = ConcurrentHashMap.newKeySet();
  private static Collection<SFXPlayback> sounds = ConcurrentHashMap.newKeySet();

  /**
   * Instantiates a new SoundEngine instance.
   * 
   * @deprecated You should never call this manually! This is already called upon startup.
   */
  @Deprecated
  public SoundEngine() {
  }

  /**
   * Gets the maximum distance from the listener at which a sound source can
   * still be heard.
   * 
   * @return The maximum distance at which a sound can be heard.
   */
  public static float getMaxDistance() {
    return maxDist;
  }

  public static void playMusic(String music) {
    playMusic(Resources.sounds().get(music));
  }

  public static void playMusic(Sound music) {
    playMusic(new LoopedTrack(music));
  }

  /**
   * Sets the currently playing track to the specified track. This has no effect if the specified track is already playing.
   *
   * @param track
   *          The track to play
   */
  public static void playMusic(Track track) {
    playMusic(track, false, true);
  }

  /**
   * Sets the currently playing track to the specified track.
   *
   * @param track
   *          The track to play
   * @param restart
   *          Whether to restart if the specified track is already playing, determined by {@link Object#equals(Object)}
   */
  public static void playMusic(Track track, boolean restart) {
    playMusic(track, false, true);
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
   */
  public static synchronized void playMusic(Track track, boolean restart, boolean stop) {
    if (!restart && music != null && music.isPlaying() && music.getTrack().equals(track)) {
      return;
    }
    MusicPlayback playback;
    try {
      playback = new MusicPlayback(track);
    } catch (LineUnavailableException e) {
      resourceFailure(e);
      return;
    }
    if (stop) {
      stopMusic();
    }
    allMusic.add(playback);
    playback.start();
    music = playback;
  }

  /**
   * Fades out the music over the specified time, if playing.
   *
   * @param time
   *          The time in frames to make the existing music fade out for if present
   */
  public static void fadeMusic(int time) {
    fadeMusic(time, null);
  }

  /**
   * Fades out the music over the specified time, then calls the provided callback.
   *
   * @param time
   *          The time in frames to make the existing music fade out for if present
   * @param callback
   *          The callback for when the fade finishes
   */
  public static synchronized void fadeMusic(final int time, final Runnable callback) {
    music = null;
    final Map<MusicPlayback, VolumeControl> faders = new HashMap<>(allMusic.size());
    for (MusicPlayback track : allMusic) {
      faders.put(track, track.createVolumeControl());
    }
    Game.loop().attach(new IUpdateable() {
      private int remaining = time;

      @Override
      public void update() {
        this.remaining--;
        if (this.remaining == 0) {
          Game.loop().detach(this);
          for (MusicPlayback track : faders.keySet()) {
            track.cancel();
          }
          if (callback != null) {
            callback.run();
          }
        } else {
          for (VolumeControl fader : faders.values()) {
            fader.set((float) this.remaining / time);
          }
        }
      }
    });
  }

  /**
   * Gets the "main" music that is playing. This usually means the last call to {@code playMusic}, though if the music has been stopped it will be
   * {@code null}.
   *
   * @return The main music, which could be {@code null}.
   */
  public static synchronized MusicPlayback getMusic() {
    return music;
  }

  public static synchronized Collection<MusicPlayback> getAllMusic() {
    return Collections.unmodifiableCollection(allMusic);
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
   * @return An {@link SFXPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public static SFXPlayback playSound(final Sound sound, final IEntity entity) {
    return playSound(sound, entity, false);
  }

  public static SFXPlayback playSound(final String sound, final IEntity entity) {
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
   * @return An {@link SFXPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public static SFXPlayback playSound(final Sound sound, final IEntity entity, boolean loop) {
    return playSound(sound, entity::getLocation, loop);
  }

  public static SFXPlayback playSound(final String sound, final IEntity entity, boolean loop) {
    return playSound(Resources.sounds().get(sound), entity, loop);
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
   * @return An {@link SFXPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public static SFXPlayback playSound(final Sound sound, final Point2D location) {
    return playSound(sound, location, false);
  }

  public static SFXPlayback playSound(final String sound, final Point2D location) {
    return playSound(Resources.sounds().get(sound), location, false);
  }

  public static SFXPlayback playSound(final Sound sound, double x, double y) {
    return playSound(sound, new Point2D.Double(x, y), false);
  }

  public static SFXPlayback playSound(final String sound, double x, double y) {
    return playSound(Resources.sounds().get(sound), new Point2D.Double(x, y), false);
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
   * @return An {@link SFXPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public static SFXPlayback playSound(final Sound sound, final Point2D location, boolean loop) {
    return playSound(sound, () -> location, loop);
  }

  public static SFXPlayback playSound(final String sound, final Point2D location, boolean loop) {
    return playSound(Resources.sounds().get(sound), location, loop);
  }

  public static SFXPlayback playSound(final Sound sound, final double x, final double y, boolean loop) {
    return playSound(sound, new Point2D.Double(x, y), loop);
  }

  public static SFXPlayback playSound(final String sound, final double x, final double y, boolean loop) {
    return playSound(Resources.sounds().get(sound), new Point2D.Double(x, y), loop);
  }

  /**
   * Plays the specified sound with the volume configured in the SOUND config
   * with a center pan.
   * 
   * @param sound
   *          The sound to play.
   * 
   * @return An {@link SFXPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public static SFXPlayback playSound(final Sound sound) {
    return playSound(sound, false);
  }

  public static SFXPlayback playSound(final String sound) {
    return playSound(Resources.sounds().get(sound), false);
  }

  /**
   * Plays the specified sound with the volume configured in the SOUND config
   * with a center pan.
   * 
   * @param sound
   *          The sound to play.
   * @param loop
   *          Determines whether this playback should be looped or not.
   * @return An {@link SFXPlayback} instance that allows to further process
   *         and control the played sound.
   */
  public static SFXPlayback playSound(final Sound sound, boolean loop) {
    return playSound(sound, () -> null, loop);
  }

  public static SFXPlayback playSound(final String sound, boolean loop) {
    return playSound(Resources.sounds().get(sound), loop);
  }

  private static SFXPlayback playSound(Sound sound, Supplier<Point2D> supplier, boolean loop) {
    Objects.requireNonNull(sound);
    SFXPlayback playback;
    try {
      playback = new SFXPlayback(sound, supplier, loop);
    } catch (LineUnavailableException e) {
      resourceFailure(e);
      return null;
    }
    playback.updateLocation(listenerLocation);
    playback.start();
    sounds.add(playback);
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
  public static void setMaxDistance(final float radius) {
    maxDist = radius;
  }

  /**
   * Stops the playback of the current background music.
   */
  public static synchronized void stopMusic() {
    for (MusicPlayback track : allMusic) {
      track.cancel();
    }
  }

  /**
   * This method allows to set the callback that is used by the SoundEngine to
   * determine where the listener location is.
   * 
   * If not explicitly set, the SoundEngine uses the camera focus (center of the
   * screen) as listener location.
   * 
   * @param callback
   *          The callback that determines the location of the sound listener.
   */
  public static void setListenerLocationCallback(Function<Point2D, Point2D> callback) {
    listenerLocationCallback = callback;
  }

  @Override
  public void start() {
    Game.inputLoop().attach(this);
    listenerLocation = Game.world().camera().getFocus();
  }

  @Override
  public void terminate() {
    Game.inputLoop().detach(this);
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
      music.setMusicVolume(Game.config().sound().getMusicVolume());
    }
  }

  private static void resourceFailure(Throwable e) {
    log.log(Level.WARNING, "could not open a line", e);
  }
}
