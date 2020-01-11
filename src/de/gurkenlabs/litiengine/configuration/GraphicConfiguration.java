package de.gurkenlabs.litiengine.configuration;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

@ConfigurationGroupInfo(prefix = "gfx_")
public class GraphicConfiguration extends ConfigurationGroup {

  private DisplayMode displayMode;

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
    this.displayMode = DisplayMode.WINDOWED;
    this.renderDynamicShadows = false;
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    this.resolutionWidth = d.width;
    this.resolutionHeight = d.height - 100;
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
    if (this.getDisplayMode() == DisplayMode.FULLSCREEN) {
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

  public boolean renderDynamicShadows() {
    return this.renderDynamicShadows;
  }

  public boolean antiAlising() {
    return this.antiAliasing;
  }

  public boolean colorInterpolation() {
    return this.colorInterpolation;
  }

  public DisplayMode getDisplayMode() {
    return displayMode;
  }

  public void setDisplayMode(DisplayMode displayMode) {
    this.set("displayMode", displayMode);
  }

  /**
   * Sets the graphic quality.
   *
   * @param graphicQuality
   *          the new graphic quality
   */
  public void setGraphicQuality(final Quality graphicQuality) {
    this.set("graphicQuality", graphicQuality);
  }

  public void setRenderDynamicShadows(final boolean renderDynamicShadows) {
    this.set("renderDynamicShadows", renderDynamicShadows);
  }

  public void setResolutionHeight(final int resolutionHeight) {
    this.set("resolutionHeight", resolutionHeight);
  }

  public void setResolutionWidth(final int resolutionWidth) {
    this.set("resolutionWidth", resolutionWidth);
  }

  public boolean enableResolutionScaling() {
    return this.enableResolutionScale;
  }

  public void setEnableResolutionScale(boolean enableResolutionScale) {
    this.set("enableResolutionScale", enableResolutionScale);
  }

  public boolean reduceFramesWhenNotFocused() {
    return this.reduceFramesWhenNotFocused;
  }

  public void setReduceFramesWhenNotFocused(boolean reduceFramesWhenNotFocused) {
    this.set("reduceFramesWhenNotFocused", reduceFramesWhenNotFocused);
  }

  public void setAntiAliasing(boolean antiAliasing) {
    this.set("antiAliasing", antiAliasing);
  }

  public void setColorInterpolation(boolean colorInterpolation) {
    this.set("colorInterpolation", colorInterpolation);
  }
}
