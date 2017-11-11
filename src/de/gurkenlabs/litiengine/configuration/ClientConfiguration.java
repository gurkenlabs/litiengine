package de.gurkenlabs.litiengine.configuration;

import java.util.Locale;

import de.gurkenlabs.configuration.ConfigurationGroup;
import de.gurkenlabs.configuration.ConfigurationGroupInfo;

/**
 * The client configuration contains client specific configuration elements.
 */
@ConfigurationGroupInfo(prefix = "CLIENT_")
public class ClientConfiguration extends ConfigurationGroup {

  private String country;

  private String language;

  private Locale locale;

  /** The max fps. */
  private int maxFps;

  private boolean showGameMetrics;

  /** The updaterate. */
  private int updaterate;

  /**
   * Instantiates a new client configuration.
   */
  public ClientConfiguration() {
    super();
    this.setUpdaterate(60);
    this.setMaxFps(60);
    this.setShowGameMetrics(false);

    this.setLanguage(Locale.getDefault().getLanguage());
    this.setCountry(Locale.getDefault().getCountry());
  }

  public String getCountry() {
    return this.country;
  }

  public String getLanguage() {
    return this.language;
  }

  public Locale getLocale() {
    if (this.locale == null) {
      this.locale = new Locale(this.getLanguage(), this.getCountry());
    }

    return this.locale;
  }

  /**
   * Gets the max fps.
   *
   * @return the max fps
   */
  public int getMaxFps() {
    return this.maxFps;
  }

  /**
   * Gets the updaterate.
   *
   * @return the updaterate
   */
  public int getUpdaterate() {
    return this.updaterate;
  }

  public void setCountry(final String country) {
    this.country = country;
    this.locale = null;
  }

  public void setLanguage(final String language) {
    this.language = language;
    this.locale = null;
  }

  /**
   * Sets the max fps.
   *
   * @param maxFps
   *          the new max fps
   */
  public void setMaxFps(final int maxFps) {
    if (maxFps < 1) {
      return;
    }

    this.maxFps = maxFps;
  }

  public void setShowGameMetrics(final boolean showGameMetrics) {
    this.showGameMetrics = showGameMetrics;
  }

  /**
   * Sets the updaterate. On a very good machine the max update rate is sth.
   * around 500 but such a high value will never be beneficial for the player.
   *
   * @param updaterate
   *          the new updaterate
   */
  public void setUpdaterate(final int updaterate) {
    if (updaterate < 1 || updaterate > 500) {
      return;
    }

    this.updaterate = updaterate;
  }

  public boolean showGameMetrics() {
    return this.showGameMetrics;
  }
}