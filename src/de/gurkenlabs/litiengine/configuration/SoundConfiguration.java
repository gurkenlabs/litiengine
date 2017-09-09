package de.gurkenlabs.litiengine.configuration;

import de.gurkenlabs.configuration.ConfigurationGroup;
import de.gurkenlabs.configuration.ConfigurationGroupInfo;

/**
 * The Class SoundSettings.
 */
@ConfigurationGroupInfo(prefix = "SOUND_")
public class SoundConfiguration extends ConfigurationGroup {

  private float musicVolume;

  /** The sound volume. */
  private float soundVolume;

  /**
   * Instantiates a new sound configuration.
   */
  public SoundConfiguration() {
    this.soundVolume = 0.5F;
    this.setMusicVolume(0.5f);
  }

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
