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

  private boolean exitOnError;

  /**
   * Constructs a new ClientConfiguration with default settings.
   */
  ClientConfiguration() {
    super();
    this.setMaxFps(60);
    this.setShowGameMetrics(false);
    this.setExitOnError(false);

    this.setLanguage(Locale.getDefault().getLanguage());
    this.setCountry(Locale.getDefault().getCountry());
  }

  /**
   * Gets the country code.
   *
   * @return the country code.
   */
  public String getCountry() {
    return this.country;
  }

  /**
   * Gets the language code.
   *
   * @return the language code.
   */
  public String getLanguage() {
    return this.language;
  }

  /**
   * Gets the locale based on the language and country codes.
   *
   * @return the locale.
   */
  public Locale getLocale() {
    if (this.getCountry() == null || this.getCountry().isEmpty()) {
      return Locale.of(this.getLanguage());
    }
    return Locale.of(this.getLanguage(), this.getCountry());
  }

  /**
   * Gets the maximum frames per second.
   *
   * @return the maximum frames per second.
   */
  public int getMaxFps() {
    return this.maxFps;
  }

  /**
   * Sets the country code.
   *
   * @param country the country code to set.
   */
  public void setCountry(final String country) {
    this.set("country", country);
  }

  /**
   * Sets the language code.
   *
   * @param language the language code to set.
   */
  public void setLanguage(final String language) {
    this.set("language", language);
  }

  /**
   * Sets the maximum frames per second.
   *
   * @param maxFps the maximum frames per second to set.
   */
  public void setMaxFps(final int maxFps) {
    this.set("maxFps", Math.max(1, maxFps));
  }

  /**
   * Sets whether to show game metrics.
   *
   * @param showGameMetrics true to show game metrics, false to hide.
   */
  public void setShowGameMetrics(final boolean showGameMetrics) {
    this.set("showGameMetrics", showGameMetrics);
  }

  /**
   * Sets whether to exit on error.
   *
   * @param exit true to exit on error, false to continue.
   */
  public void setExitOnError(boolean exit) {
    this.set("exitOnError", exit);
  }

  /**
   * Checks if game metrics are shown.
   *
   * @return true if game metrics are shown, false otherwise.
   */
  public boolean showGameMetrics() {
    return this.showGameMetrics;
  }

  /**
   * Checks if the application exits on error.
   *
   * @return true if the application exits on error, false otherwise.
   */
  public boolean exitOnError() {
    return this.exitOnError;
  }
}
