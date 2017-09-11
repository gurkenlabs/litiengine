package de.gurkenlabs.litiengine.sound;

import java.awt.geom.Point2D;

import de.gurkenlabs.core.ILaunchable;
import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * This interface provides all methods to playback sounds and music in your
 * game. It allows to define the 2D coordinates of the sound or even pass in the
 * source entity of the sound which will adjust the position accoring to the
 * position of the entity. The LILI Engine Sound Engine supports .wav, .mp3 and
 * .ogg by default. If you need other file extensions, you have to write an own
 * SPI implementation and inject it in your project.
 */
public interface ISoundEngine extends ILaunchable {

  /**
   * Gets the maximum distance from the listener at which a sound source can
   * still be heared.
   * 
   * @return
   */
  public float getMaxDistance();

  /**
   * Loops the specified sound file as background music. If another music was
   * specified beforehand, its playback will get interrupted and the new one
   * will be played.
   * 
   * @param sound
   */
  public void playMusic(Sound sound);

  /**
   * Plays the specified sound and updates its volume and pan by the current
   * entity location in relation to the listener location.
   * 
   * @param entity
   * @param sound
   */
  public void playSound(IEntity entity, Sound sound);

  /**
   * Plays the specified sound at the specified location and updates the volume
   * and pan in relation to the listener location.
   * 
   * @param location
   * @param sound
   */
  public void playSound(Point2D location, Sound sound);

  /**
   * Plays the specified sound with the volume configured in the SOUND config
   * with a center pan.
   * 
   * @param sound
   */
  public void playSound(Sound sound);

  /**
   * Sets the maximum distance from the listener at which a sound source can
   * still be heared. If the distance between the sound source and the listener
   * is greater than the specified value, the volume is set to 0.
   * 
   * @param distance
   */
  public void setMaxDistance(float distance);

  /**
   * Stops the playback of the current background music.
   */
  public void stopMusic();
}
