package de.gurkenlabs.litiengine.configuration;

@ConfigurationGroupInfo(prefix = "sfx_")
public class SoundConfiguration extends ConfigurationGroup {

  private float musicVolume;

  private float soundVolume;

  SoundConfiguration() {
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
    this.set("musicVolume", musicVolume);
  }

  /**
   * Sets the sound volume.
   *
   * @param soundVolume
   *          the new sound volume
   */
  public void setSoundVolume(final float soundVolume) {
    this.set("soundVolume", soundVolume);
  }
}
