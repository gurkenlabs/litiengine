/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.configuration;

import de.gurkenlabs.annotation.ConfigurationGroupInfo;
import de.gurkenlabs.configuration.ConfigurationGroup;

// TODO: Auto-generated Javadoc
/**
 * The Class ClientConfiguration.
 */
@ConfigurationGroupInfo(prefix = "CLIENT_")
public class ClientConfiguration extends ConfigurationGroup {

  /** The max fps. */
  private int maxFps;

  /** The updaterate. */
  private int updaterate;

  private boolean showGameMetrics;

  /**
   * Instantiates a new client configuration.
   */
  public ClientConfiguration() {
    super();
    this.setUpdaterate(23);
    this.setMaxFps(60);
    this.setShowGameMetrics(false);
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