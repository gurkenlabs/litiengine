/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.configuration;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import de.gurkenlabs.annotation.ConfigurationGroupInfo;
import de.gurkenlabs.configuration.ConfigurationGroup;
import de.gurkenlabs.configuration.Quality;

// TODO: Auto-generated Javadoc
/**
 * The Class GraphicSettings contains all settings that allow to influence the
 * appearance of the game.
 */
@ConfigurationGroupInfo(prefix = "GRAPHIC_")
public class GraphicConfiguration extends ConfigurationGroup {

  /** The fullscreen. */
  private boolean fullscreen;

  /** The graphic quality. */
  private Quality graphicQuality;

  private boolean renderDynamicShadows;

  /** The resolution. */
  private int resolutionHeight;

  private int resolutionWidth;

  /**
   * Instantiates a new graphic configuration.
   */
  public GraphicConfiguration() {
    this.graphicQuality = Quality.LOW;
    this.fullscreen = false;
    this.renderDynamicShadows = false;
    this.resolutionHeight = 900;
    this.resolutionWidth = 1600;
  }

  /**
   * Gets the graphic quality.
   *
   * @return the graphic quality
   */
  public Quality getGraphicQuality() {
    return this.graphicQuality;
  }

  /**
   * Gets the resolution.
   *
   * @return the resolution
   */
  public Dimension getResolution() {
    if (this.isFullscreen()) {
      final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      final int width = gd.getDisplayMode().getWidth();
      final int height = gd.getDisplayMode().getHeight();
      return new Dimension(width, height);
    }

    return new Dimension(this.resolutionWidth, this.resolutionHeight);
  }

  public int getResolutionHeight() {
    return this.resolutionHeight;
  }

  public int getResolutionWidth() {
    return this.resolutionWidth;
  }

  /**
   * Checks if is fullscreen.
   *
   * @return true, if is fullscreen
   */
  public boolean isFullscreen() {
    return this.fullscreen;
  }

  public boolean renderDynamicShadows() {
    return this.renderDynamicShadows;
  }

  /**
   * Sets the fullscreen.
   *
   * @param fullscreen
   *          the new fullscreen
   */
  public void setFullscreen(final boolean fullscreen) {
    if (fullscreen == this.fullscreen) {
      return;
    }

    this.fullscreen = fullscreen;
  }

  /**
   * Sets the graphic quality.
   *
   * @param graphicQuality
   *          the new graphic quality
   */
  public void setGraphicQuality(final Quality graphicQuality) {
    this.graphicQuality = graphicQuality;
  }

  public void setRenderDynamicShadows(final boolean renderDynamicShadows) {
    this.renderDynamicShadows = renderDynamicShadows;
  }

  public void setResolutionHeight(final int resolutionHeight) {
    this.resolutionHeight = resolutionHeight;
  }

  public void setResolutionWidth(final int resolutionWidth) {
    this.resolutionWidth = resolutionWidth;
  }
}
