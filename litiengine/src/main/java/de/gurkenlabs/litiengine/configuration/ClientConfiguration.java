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
   * Instantiates a new client configuration.
   */
  ClientConfiguration() {
    super();
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
      return Locale.of(this.getLanguage());
    }
    return Locale.of(this.getLanguage(), this.getCountry());
  }

  /**
   * Gets the max fps.
   *
   * @return the max fps
   */
  public int getMaxFps() {
    return this.maxFps;
  }

  public void setCountry(final String country) {
    this.set("country", country);
  }

  public void setLanguage(final String language) {
    this.set("language", language);
  }

  /**
   * Sets the max fps.
   *
   * @param maxFps
   *          the new max fps
   */
  public void setMaxFps(final int maxFps) {
    this.set("maxFps", Math.max(1, maxFps));
  }

  public void setShowGameMetrics(final boolean showGameMetrics) {
    this.set("showGameMetrics", showGameMetrics);
  }

  public void setExitOnError(boolean exit) {
    this.set("exitOnError", exit);
  }

  public boolean showGameMetrics() {
    return this.showGameMetrics;
  }

  public boolean exitOnError() {
    return this.exitOnError;
  }
}
