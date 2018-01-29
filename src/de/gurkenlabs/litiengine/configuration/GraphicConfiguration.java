package de.gurkenlabs.litiengine.configuration;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import de.gurkenlabs.configuration.ConfigurationGroup;
import de.gurkenlabs.configuration.ConfigurationGroupInfo;
import de.gurkenlabs.configuration.Quality;

/**
 * The Class GraphicSettings contains all settings that allow to influence the
 * appearance of the game.
 */
@ConfigurationGroupInfo(prefix = "gfx_")
public class GraphicConfiguration extends ConfigurationGroup {

  /** The fullscreen. */
  private boolean fullscreen;

  /** The graphic quality. */
  private Quality graphicQuality;

  private boolean renderDynamicShadows;

  // TODO: on low performance machines (surface 2 pro tablet) this can lead to
  // frame drops (65->35fps); on high performance machines this can increase frame
  // rate...
  // Possible explanation: on low performance machines it takes more computing
  // time to render larger images?
  private boolean cacheStaticTiles;

  /** The resolution. */
  private int resolutionHeight;

  private int resolutionWidth;

  private boolean enableResolutionScale;

  private boolean reduceFramesWhenNotFocused;

  /**
   * Instantiates a new graphic configuration.
   */
  public GraphicConfiguration() {
    this.graphicQuality = Quality.LOW;
    this.fullscreen = false;
    this.renderDynamicShadows = false;
    this.resolutionHeight = 900;
    this.resolutionWidth = 1600;
    this.setCacheStaticTiles(true);
    this.setEnableResolutionScale(true);
    this.setReduceFramesWhenNotFocused(true);
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

  public boolean enableCacheStaticTiles() {
    return this.cacheStaticTiles;
  }

  public void setCacheStaticTiles(boolean cacheStaticTiles) {
    this.cacheStaticTiles = cacheStaticTiles;
  }

  public boolean enableResolutionScale() {
    return this.enableResolutionScale;
  }

  public void setEnableResolutionScale(boolean enableResolutionScale) {
    this.enableResolutionScale = enableResolutionScale;
  }

  public boolean reduceFramesWhenNotFocused() {
    return this.reduceFramesWhenNotFocused;
  }

  public void setReduceFramesWhenNotFocused(boolean reduceFramesWhenNotFocused) {
    this.reduceFramesWhenNotFocused = reduceFramesWhenNotFocused;
  }
}
