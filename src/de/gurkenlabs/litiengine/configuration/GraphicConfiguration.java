package de.gurkenlabs.litiengine.configuration;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

@ConfigurationGroupInfo(prefix = "gfx_")
public class GraphicConfiguration extends ConfigurationGroup {

  private boolean fullscreen;

  private Quality graphicQuality;

  private boolean renderDynamicShadows;

  private int resolutionHeight;

  private int resolutionWidth;

  private boolean enableResolutionScale;

  private boolean reduceFramesWhenNotFocused;

  private boolean antiAliasing;

  private boolean colorInterpolation;

  /**
   * Instantiates a new graphic configuration.
   */
  public GraphicConfiguration() {
    this.graphicQuality = Quality.LOW;
    this.fullscreen = false;
    this.renderDynamicShadows = false;
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    this.resolutionHeight = d.getWidth();
    this.resolutionWidth = d.getHeight() - 100;
    this.setEnableResolutionScale(true);
    this.setReduceFramesWhenNotFocused(true);
    this.setAntiAliasing(false);
    this.setColorInterpolation(false);
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

  public boolean antiAlising() {
    return this.antiAliasing;
  }

  public boolean colorInterpolation() {
    return this.colorInterpolation;
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

  public boolean enableResolutionScaling() {
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

  public void setAntiAliasing(boolean antiAliasing) {
    this.antiAliasing = antiAliasing;
  }

  public void setColorInterpolation(boolean colorInterpolation) {
    this.colorInterpolation = colorInterpolation;
  }
}
