package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.configuration.SoundConfiguration;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.Sounds;
import de.gurkenlabs.litiengine.sound.SFXPlayback;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.sound.SoundEngine;

/**
 * This is an Entity that can play or loop ambient sound effects within a given range and with a given volume.
 */
@EntityInfo(renderType = RenderType.OVERLAY)
@CollisionInfo(collision = false, collisionType = Collision.NONE)
@TmxType(MapObjectType.SOUNDSOURCE)
public class SoundSource extends Entity {

  private float volume;
  private int range;

  private boolean loop;
  private Sound sound;
  private SFXPlayback playback;

  /**
   * An empty constructor that just calls the super constructor of {@link Entity}.
   */
  public SoundSource() {
    super();
  }

  /**
   * Initialize a SoundSource with a Sound.
   * 
   * @param sound
   *          The sound used for playback
   */
  public SoundSource(Sound sound) {
    this.setSound(sound);
  }

  /**
   * Initialize a SoundSource with the name of a sound which will then be fetched from the {@code Resources.sounds()}.
   * 
   * @param name
   *          The name of the sound used for playback
   * @see Sounds#get(String)
   */
  public SoundSource(String name) {
    this.setSound(name);
  }

  /**
   * Initialize a SoundSource at a certain location.
   * 
   * @param x
   *          The x coordinate
   * @param y
   *          The y coordinate
   */
  public SoundSource(double x, double y) {
    this.setX(x);
    this.setY(y);
  }

  /**
   * Initialize a SoundSource at a certain location with a given size.
   * The size is irrelevant for the sound playback.
   * 
   * @param x
   *          The x coordinate
   * @param y
   *          The y coordinate
   * @param width
   *          The entity width
   * @param height
   *          The entity height
   */
  public SoundSource(double x, double y, double width, double height) {
    this(x, y);
    this.setWidth(width);
    this.setHeight(height);
  }

  /**
   * Get the volume modifier. The volume modifier is multiplied with the global sound volume defined by {@link SoundConfiguration#getSoundVolume()}.
   * 
   * @return a float determining how much louder or quieter the sound is played back. 1.0 is the standard playback volume.
   */
  public float getVolume() {
    return volume;
  }

  /**
   * Set the volume modifier. The volume modifier is multiplied with the global sound volume defined by {@link SoundConfiguration#getSoundVolume()}.
   * 
   * @param volume
   *          a float determining how much louder or quieter the sound is played back. 1.0 is the standard playback volume.
   */
  public void setVolume(float volume) {
    this.volume = volume;
  }

  /**
   * Boolean determining if the sound is looped or only played back once.
   * 
   * @return {@code true}, if the sound is looped when calling {@link #play()}. {@code false}, if it is played back just once.
   */
  public boolean isLoop() {
    return loop;
  }

  /**
   * Toggles looping for the sound playback.
   * 
   * @param loop
   *          {@code true}, if the sound should be looped when calling {@link #play()}. {@code false}, if it should be played back just once.
   */
  public void setLoop(boolean loop) {
    this.loop = loop;
  }

  /**
   * The sound to be played.
   * 
   * @return the sound instance used for playback.
   */
  public Sound getSound() {
    return sound;
  }

  /**
   * The playback used for playing the sound.
   * 
   * @return the playback instance.
   */
  public SFXPlayback getPlayback() {
    return this.playback;
  }

  /**
   * The name of the currently set sound.
   * 
   * @return A String containing the sound name.
   */
  public String getSoundName() {
    return this.sound.getName();
  }

  /**
   * The range in pixels for which the sound can be heard.
   * 
   * @return an {@code int} representing the range in pixels.
   */
  public int getRange() {
    return range;
  }

  /**
   * Sets the range in pixels for which the sound can be heard.
   * 
   * @param range
   *          an {@code int} representing the range in pixels.
   */
  public void setRange(int range) {
    this.range = range;
  }

  /**
   * Sets the sound by fetching a sound resource with a given name.
   * 
   * @param name
   *          The name of the Sound resource.
   * @see Sounds#get(String)
   */
  public void setSound(String name) {
    this.sound = Resources.sounds().get(name);
  }

  /**
   * Sets the sound to be played.
   * 
   * @param sound
   *          The sound to be played
   */
  public void setSound(Sound sound) {
    this.sound = sound;
  }

  /**
   * Starts a new playback in the SoundEngine and saves a reference to it in the SoundSource instance. The playback reference can be called with
   * {@link #getPlayback()}.
   * 
   * @see SoundEngine#playSound()
   * @see SFXPlayback
   */
  public void play() {
    this.playback = Game.audio().playSound(this.getSound(), this, this.isLoop(), this.getRange(), this.getVolume());
  }

  /**
   * Pauses the current playback.
   * 
   * @see SFXPlayback#pausePlayback()
   */
  public void pause() {
    this.getPlayback().pausePlayback();
  }

  /**
   * Resumes the current playback if it was paused.
   * 
   * @see SFXPlayback#resumePlayback()
   */
  public void resume() {
    this.getPlayback().resumePlayback();
  }

  /**
   * Cancels the current playback.
   * 
   * @see SFXPlayback#cancel()
   */
  public void stop() {
    this.getPlayback().cancel();
  }

}
