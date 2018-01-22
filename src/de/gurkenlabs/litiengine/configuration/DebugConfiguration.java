package de.gurkenlabs.litiengine.configuration;

import de.gurkenlabs.configuration.ConfigurationGroup;
import de.gurkenlabs.configuration.ConfigurationGroupInfo;
import de.gurkenlabs.litiengine.Game;

@ConfigurationGroupInfo(prefix = "dbg_", debug = true)
public class DebugConfiguration extends ConfigurationGroup {
  private boolean debugEnabled = false;

  private boolean renderAStarInfo = false;

  private boolean renderBoundingBoxes = false;

  /** The render collision boxes. */
  private boolean renderCollisionBoxes = false;

  private boolean renderDebugMouse = false;

  /** The render entity names. */
  private boolean renderEntityNames = false;

  private boolean renderGuiComponentBoundingBoxes = false;

  /** The render hit boxes. */
  private boolean renderHitBoxes = false;

  /** The render paths. */
  private boolean renderPaths = false;
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

  public boolean isRenderAStarInfo() {
    return this.renderAStarInfo;
  }

  public boolean isRenderDebugMouse() {
    return this.isDebugEnabled() && this.renderDebugMouse;
  }

  public boolean renderBoundingBoxes() {
    return this.isDebugEnabled() && this.renderBoundingBoxes;
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

  /**
   * Render paths.
   *
   * @return true, if successful
   */
  public boolean renderPaths() {
    return this.isDebugEnabled() && this.renderPaths;
  }

  public boolean renderGuiComponentBoundingBoxes() {
    return renderGuiComponentBoundingBoxes;
  }

  public void setDebugEnabled(final boolean debugEnabled) {
    this.debugEnabled = Game.DEBUG && debugEnabled;
  }

  public void setRenderAStarInfo(final boolean renderAStarInfo) {
    this.renderAStarInfo = renderAStarInfo;
  }

  public void setRenderBoundingBoxes(final boolean renderBoundingBoxes) {
    this.renderBoundingBoxes = renderBoundingBoxes;
  }

  public void setRenderCollisionBoxes(final boolean renderCollisionBoxes) {
    this.renderCollisionBoxes = renderCollisionBoxes;
  }

  public void setRenderDebugMouse(final boolean renderDebugMouse) {
    this.renderDebugMouse = renderDebugMouse;
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

  public void setRenderGuiComponentBoundingBoxes(boolean renderGuiComponentBoundingBoxes) {
    this.renderGuiComponentBoundingBoxes = renderGuiComponentBoundingBoxes;
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
}
