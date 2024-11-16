package de.gurkenlabs.litiengine.configuration;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

/**
 * Represents the graphic configuration settings. This class extends the ConfigurationGroup to provide specific settings for graphics.
 */
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
   * Constructs a new GraphicConfiguration with default settings.
   */
  GraphicConfiguration() {
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


  public Quality getGraphicQuality() {
    return graphicQuality;
  }


  public Dimension getResolution() {
    if (this.getDisplayMode() == DisplayMode.FULLSCREEN) {
      final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      final int width = gd.getDisplayMode().getWidth();
      final int height = gd.getDisplayMode().getHeight();
      return new Dimension(width, height);
    }

    return new Dimension(this.resolutionWidth, this.resolutionHeight);
  }

  /**
   * Gets the current resolution height.
   *
   * @return the resolution height.
   */
  public int getResolutionHeight() {
    return resolutionHeight;
  }

  /**
   * Gets the current resolution width.
   *
   * @return the resolution width.
   */
  public int getResolutionWidth() {
    return resolutionWidth;
  }

  /**
   * Checks if dynamic shadows rendering is enabled.
   *
   * @return true if dynamic shadows rendering is enabled, false otherwise.
   */
  public boolean renderDynamicShadows() {
    return renderDynamicShadows;
  }

  /**
   * Checks if anti-aliasing is enabled.
   *
   * @return true if anti-aliasing is enabled, false otherwise.
   */
  public boolean antiAliasing() {
    return antiAliasing;
  }

  /**
   * Checks if color interpolation is enabled.
   *
   * @return true if color interpolation is enabled, false otherwise.
   */
  public boolean colorInterpolation() {
    return colorInterpolation;
  }

  /**
   * Gets the current display mode.
   *
   * @return the display mode.
   */
  public DisplayMode getDisplayMode() {
    return displayMode;
  }

  /**
   * Sets the display mode.
   *
   * @param displayMode the new display mode.
   */
  public void setDisplayMode(DisplayMode displayMode) {
    this.set("displayMode", displayMode);
  }

  /**
   * Sets the graphic quality.
   *
   * @param graphicQuality the new graphic quality.
   */
  public void setGraphicQuality(final Quality graphicQuality) {
    this.set("graphicQuality", graphicQuality);
  }

  /**
   * Sets whether to render dynamic shadows.
   *
   * @param renderDynamicShadows true to enable dynamic shadows rendering, false to disable.
   */
  public void setRenderDynamicShadows(final boolean renderDynamicShadows) {
    this.set("renderDynamicShadows", renderDynamicShadows);
  }

  /**
   * Sets the resolution height.
   *
   * @param resolutionHeight the new resolution height.
   */
  public void setResolutionHeight(final int resolutionHeight) {
    this.set("resolutionHeight", resolutionHeight);
  }

  /**
   * Sets the resolution width.
   *
   * @param resolutionWidth the new resolution width.
   */
  public void setResolutionWidth(final int resolutionWidth) {
    this.set("resolutionWidth", resolutionWidth);
  }

  /**
   * Checks if resolution scaling is enabled.
   *
   * @return true if resolution scaling is enabled, false otherwise.
   */
  public boolean enableResolutionScaling() {
    return enableResolutionScale;
  }

  /**
   * Sets whether to enable resolution scaling.
   *
   * @param enableResolutionScale true to enable resolution scaling, false to disable.
   */
  public void setEnableResolutionScale(boolean enableResolutionScale) {
    this.set("enableResolutionScale", enableResolutionScale);
  }

  /**
   * Checks if frames should be reduced when not focused.
   *
   * @return true if frames should be reduced when not focused, false otherwise.
   */
  public boolean reduceFramesWhenNotFocused() {
    return reduceFramesWhenNotFocused;
  }

  /**
   * Sets whether to reduce frames when not focused.
   *
   * @param reduceFramesWhenNotFocused true to reduce frames when not focused, false to not reduce.
   */
  public void setReduceFramesWhenNotFocused(boolean reduceFramesWhenNotFocused) {
    this.set("reduceFramesWhenNotFocused", reduceFramesWhenNotFocused);
  }

  /**
   * Sets whether to enable anti-aliasing.
   *
   * @param antiAliasing true to enable anti-aliasing, false to disable.
   */
  public void setAntiAliasing(boolean antiAliasing) {
    this.set("antiAliasing", antiAliasing);
  }

  /**
   * Sets whether to enable color interpolation.
   *
   * @param colorInterpolation true to enable color interpolation, false to disable.
   */
  public void setColorInterpolation(boolean colorInterpolation) {
    this.set("colorInterpolation", colorInterpolation);
  }
}
