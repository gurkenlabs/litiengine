package de.gurkenlabs.litiengine.configuration;

import java.util.Locale;

/**
 * The client configuration contains client specific configuration elements.
 */
@ConfigurationGroupInfo(prefix = "cl_")
public class ClientConfiguration extends ConfigurationGroup {

  private String country;

  private String language;

  private int maxFps;

  private boolean showGameMetrics;

  private int updaterate;

  private boolean exitOnError;

  /**
   * Instantiates a new client configuration.
   */
  public ClientConfiguration() {
    super();
    this.setUpdaterate(60);
    this.setMaxFps(60);
    this.setShowGameMetrics(false);
    this.setExitOnError(false);

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
    if (this.getCountry() == null || this.getCountry().isEmpty()) {
      return new Locale(this.getLanguage());
    }
    return new Locale(this.getLanguage(), this.getCountry());
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
  }

  public void setLanguage(final String language) {
    this.language = language;
  }

  /**
   * Sets the max fps.
   *
   * @param maxFps
   *          the new max fps
   */
  public void setMaxFps(final int maxFps) {
    this.maxFps = Math.max(1, maxFps);
  }

  public void setShowGameMetrics(final boolean showGameMetrics) {
    this.showGameMetrics = showGameMetrics;
  }

  /**
   * Sets the updaterate. On a very good machine the max update rate is sth.
   * around 500 but such a high value will never be beneficial for the player.
   * 
   * <p>
   * This defaults to a value of 60.
   * </p>
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

  public void setExitOnError(boolean exit) {
    this.exitOnError = exit;
  }

  public boolean showGameMetrics() {
    return this.showGameMetrics;
  }

  public boolean exitOnError() {
    return this.exitOnError;
  }
}