/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.configuration;

import de.gurkenlabs.annotation.ConfigurationGroupInfo;
import de.gurkenlabs.configuration.ConfigurationGroup;

// TODO: Auto-generated Javadoc
/**
 * The Class SoundSettings.
 */
@ConfigurationGroupInfo(prefix = "SOUND_")
public class SoundConfiguration extends ConfigurationGroup {

  /** The music volume. */
  private float musicVolume;

  /** The sound volume. */
  private float soundVolume;

  /**
   * Instantiates a new sound configuration.
   */
  public SoundConfiguration() {
    this.musicVolume = 0.0F;
    this.soundVolume = 0.5F;
  }

  /**
   * Gets the music volume.
   *
   * @return the music volume
   */
  public float getMusicVolume() {
    return this.musicVolume;
  }

  /**
   * Gets the sound volume.
   *
   * @return the sound volume
   */
  public float getSoundVolume() {
    return this.soundVolume;
  }

  /**
   * Sets the music volume.
   *
   * @param musicVolume
   *          the new music volume
   */
  public void setMusicVolume(final float musicVolume) {
    this.musicVolume = musicVolume;
  }

  /**
   * Sets the sound volume.
   *
   * @param soundVolume
   *          the new sound volume
   */
  public void setSoundVolume(final float soundVolume) {
    this.soundVolume = soundVolume;
  }
}
