package de.gurkenlabs.litiengine.configuration;

@ConfigurationGroupInfo(prefix = "sfx_")
public class SoundConfiguration extends ConfigurationGroup {

  private float musicVolume;

  private float soundVolume;

  /**
   * Constructs a new SoundConfiguration with default volume settings.
   */
  SoundConfiguration() {
    this.setSoundVolume(0.5f);
    this.setMusicVolume(0.5f);
  }

  /**
   * Gets the current music volume.
   *
   * @return the music volume.
   */
  public float getMusicVolume() {
    return this.musicVolume;
  }

  /**
   * Gets the current sound volume.
   *
   * @return the sound volume.
   */
  public float getSoundVolume() {
    return this.soundVolume;
  }

  /**
   * Sets the music volume.
   *
   * @param musicVolume the new music volume.
   */
  public void setMusicVolume(final float musicVolume) {
    this.set("musicVolume", musicVolume);
  }

  /**
   * Sets the sound volume.
   *
   * @param soundVolume the new sound volume.
   */
  public void setSoundVolume(final float soundVolume) {
    this.set("soundVolume", soundVolume);
  }
}
