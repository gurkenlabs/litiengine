package de.gurkenlabs.litiengine.configuration;

import de.gurkenlabs.annotation.ConfigurationGroupInfo;
import de.gurkenlabs.configuration.ConfigurationGroup;

@ConfigurationGroupInfo(prefix = "DEBUG_")
public class DebugConfiguration extends ConfigurationGroup {
  private boolean debugEnabled = false;

  /** The render collision boxes. */
  private boolean renderCollisionBoxes = false;

  /** The render entity names. */
  private boolean renderEntityNames = false;

  /** The render hit boxes. */
  private boolean renderHitBoxes = false;

  /** The render paths. */
  private boolean renderPaths = false;

  private boolean renderAStarInfo = false;

  /** The show mouse target metric. */
  private boolean showMouseTargetMetric = true;

  /** The show tiles metric. */
  private boolean showTilesMetric = false;

  /**
   * Checks if is debug enabled.
   *
   * @return true, if is debug enabled
   */
  public boolean isDebugEnabled() {
    return this.debugEnabled;
  }

  /**
   * Render collision boxes.
   *
   * @return true, if successful
   */
  public boolean renderCollisionBoxes() {
    return this.isDebugEnabled() && this.renderCollisionBoxes;
  }

  /**
   * Render entity names.
   *
   * @return true, if successful
   */
  public boolean renderEntityNames() {
    return this.isDebugEnabled() && this.renderEntityNames;
  }

  /**
   * Render hit boxes.
   *
   * @return true, if successful
   */
  // Render settings
  public boolean renderHitBoxes() {
    return this.isDebugEnabled() && this.renderHitBoxes;
  }

  public void setDebugEnabled(boolean debugEnabled) {
    this.debugEnabled = debugEnabled;
  }

  /**
   * Render paths.
   *
   * @return true, if successful
   */
  public boolean renderPaths() {
    return this.isDebugEnabled() && this.renderPaths;
  }

  public void setRenderCollisionBoxes(final boolean renderCollisionBoxes) {
    this.renderCollisionBoxes = renderCollisionBoxes;
  }

  public void setRenderEntityNames(final boolean renderEntityNames) {
    this.renderEntityNames = renderEntityNames;
  }

  public void setRenderHitBoxes(final boolean renderHitBoxes) {
    this.renderHitBoxes = renderHitBoxes;
  }

  public void setRenderPaths(final boolean renderPaths) {
    this.renderPaths = renderPaths;
  }

  public void setShowMouseTargetMetric(final boolean showMouseTargetMetric) {
    this.showMouseTargetMetric = showMouseTargetMetric;
  }

  public void setShowTilesMetric(final boolean showTilesMetric) {
    this.showTilesMetric = showTilesMetric;
  }

  /**
   * Show mouse target metric.
   *
   * @return true, if successful
   */
  public boolean showMouseTargetMetric() {
    return this.isDebugEnabled() && this.showMouseTargetMetric;
  }

  /**
   * Show tiles metric.
   *
   * @return true, if successful
   */
  // game metric settings
  public boolean showTilesMetric() {
    return this.isDebugEnabled() && this.showTilesMetric;
  }

  public boolean isRenderAStarInfo() {
    return this.renderAStarInfo;
  }

  public void setRenderAStarInfo(final boolean renderAStarInfo) {
    this.renderAStarInfo = renderAStarInfo;
  }
}
