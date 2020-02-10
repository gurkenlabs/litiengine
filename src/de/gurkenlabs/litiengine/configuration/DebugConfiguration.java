package de.gurkenlabs.litiengine.configuration;

import de.gurkenlabs.litiengine.Game;

@ConfigurationGroupInfo(prefix = "dbg_", debug = true)
public class DebugConfiguration extends ConfigurationGroup {
  private boolean debugEnabled = false;

  private boolean renderBoundingBoxes = false;

  private boolean renderCollisionBoxes = false;

  private boolean renderDebugMouse = false;

  private boolean renderEntityNames = false;

  private boolean renderGuiComponentBoundingBoxes = false;

  private boolean renderHitBoxes = false;

  private boolean showMouseTargetMetric = true;

  private boolean showTilesMetric = false;

  private boolean trackRenderTimes = false;

  DebugConfiguration() {
    super();
  }
  
  /**
   * Checks if is debug enabled.
   *
   * @return true, if is debug enabled
   */
  public boolean isDebugEnabled() {
    return Game.isDebug() && this.debugEnabled;
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

  public boolean renderGuiComponentBoundingBoxes() {
    return this.isDebugEnabled() && this.renderGuiComponentBoundingBoxes;
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

  public boolean trackRenderTimes() {
    return this.isDebugEnabled() && this.trackRenderTimes;
  }
  
  public void setDebugEnabled(final boolean debugEnabled) {
    this.set("debugEnabled", debugEnabled);
  }

  public void setRenderBoundingBoxes(final boolean renderBoundingBoxes) {
    this.set("renderBoundingBoxes", renderBoundingBoxes);
  }

  public void setRenderCollisionBoxes(final boolean renderCollisionBoxes) {
    this.set("renderCollisionBoxes", renderCollisionBoxes);
  }

  public void setRenderDebugMouse(final boolean renderDebugMouse) {
    this.set("renderDebugMouse", renderDebugMouse);
  }

  public void setRenderEntityNames(final boolean renderEntityNames) {
    this.set("renderEntityNames", renderEntityNames);
  }

  public void setRenderHitBoxes(final boolean renderHitBoxes) {
    this.set("renderHitBoxes", renderHitBoxes);
  }

  public void setShowMouseTargetMetric(final boolean showMouseTargetMetric) {
    this.set("showMouseTargetMetric", showMouseTargetMetric);
  }

  public void setShowTilesMetric(final boolean showTilesMetric) {
    this.set("showTilesMetric", showTilesMetric);
  }

  public void setRenderGuiComponentBoundingBoxes(boolean renderGuiComponentBoundingBoxes) {
    this.set("renderGuiComponentBoundingBoxes", renderGuiComponentBoundingBoxes);
  }

  public void setTrackRenderTimes(boolean trackRenderTimes) {
    this.set("trackRenderTimes", trackRenderTimes);
  }
}
