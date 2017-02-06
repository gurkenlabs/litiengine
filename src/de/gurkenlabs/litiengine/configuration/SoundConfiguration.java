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

  /** The sound volume. */
  private float soundVolume;
  
  private float musicVolume;

  /**
   * Instantiates a new sound configuration.
   */
  public SoundConfiguration() {
    this.soundVolume = 0.5F;
    this.setMusicVolume(0.5f);
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
   * Sets the sound volume.
   *
   * @param soundVolume
   *          the new sound volume
   */
  public void setSoundVolume(final float soundVolume) {
    this.soundVolume = soundVolume;
  }

  public float getMusicVolume() {
    return musicVolume;
  }

  public void setMusicVolume(float musicVolume) {
    this.musicVolume = musicVolume;
  }
}
