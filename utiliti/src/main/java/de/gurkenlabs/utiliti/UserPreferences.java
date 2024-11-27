package de.gurkenlabs.utiliti;

import de.gurkenlabs.litiengine.configuration.ConfigurationGroup;
import de.gurkenlabs.litiengine.configuration.ConfigurationGroupInfo;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.Style.Theme;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the user preferences for the UtiLITI application. This class extends the ConfigurationGroup and provides various settings that can be
 * configured by the user, such as UI scale, grid settings, and theme.
 */
@ConfigurationGroupInfo(prefix = "user_")
public class UserPreferences extends ConfigurationGroup {
  public static final float UI_SCALE_MAX = 2.0f;
  public static final float UI_SCALE_MIN = 0.5f;

  private float zoom;
  private boolean showGrid;
  private boolean clampToMap;
  private boolean snapToPixels;
  private boolean snapToGrid;
  private boolean renderBoundingBoxes;
  private boolean renderCustomMapObjects;
  private boolean renderMapIds;
  private boolean renderNames;
  private boolean compressFile;
  private boolean syncMaps;
  private int frameState;
  private int mainSplitter;
  private int selectionEditSplitter;
  private int mapPanelSplitter;
  private int bottomSplitter;
  private int assetsSplitter;
  private int width;
  private int height;

  private float gridLineWidth;
  private String gridColor;
  private int snapDivision;

  private String lastGameFile;
  private String[] lastOpenedFiles;
  private float uiScale;

  private Theme theme;

  /**
   * Constructs a new UserPreferences object with default settings. Initializes various user preference settings such as zoom, grid visibility,
   * snapping options, rendering options, and theme.
   */
  public UserPreferences() {
    this.zoom = 1.0f;
    this.showGrid = true;
    this.clampToMap = true;
    this.snapToPixels = true;
    this.snapToGrid = true;
    this.renderBoundingBoxes = true;
    this.renderNames = true;
    this.lastGameFile = "";
    this.lastOpenedFiles = new String[10];
    this.compressFile = false;
    this.gridLineWidth = 1.0f;
    this.gridColor = ColorHelper.encode(Style.COLOR_DEFAULT_GRID);
    this.snapDivision = 1;
    this.setUiScale(1.0f);
    this.setTheme(Theme.DARK);
  }

  /**
   * Adds a file to the list of last opened files. Ensures that the list contains a maximum of 10 elements and removes duplicates.
   *
   * @param str the file to be added to the list of last opened files.
   */
  public void addOpenedFile(String str) {
    // ensure max 10 elements
    List<String> newFiles = new ArrayList<>();
    for (int i = 0; i <= this.lastOpenedFiles.length; i++) {
      newFiles.add(null);
    }

    // make space for the new element and clear all duplicates
    for (int i = 1; i < this.lastOpenedFiles.length; i++) {
      if (this.lastOpenedFiles[i - 1] != null && this.lastOpenedFiles[i - 1].equals(str)) {
        continue;
      }

      if (this.lastOpenedFiles[i - 1] == null || this.lastOpenedFiles[i - 1].equals("null")) {
        newFiles.add(i, null);
      } else {
        newFiles.add(i, this.lastOpenedFiles[i - 1]);
      }
    }

    // add the new element
    newFiles.addFirst(str);
    newFiles.removeAll(Collections.singleton(null));
    // clear array
    this.lastOpenedFiles = new String[10];

    // fill array
    for (int i = 0; i < newFiles.size(); i++) {
      this.lastOpenedFiles[i] = newFiles.get(i);
    }
  }

  /**
   * Gets the current zoom level.
   *
   * @return the current zoom level.
   */
  public float getZoom() {
    return this.zoom;
  }

  /**
   * Sets the zoom level.
   *
   * @param zoom the new zoom level.
   */
  public void setZoom(float zoom) {
    this.zoom = zoom;
  }

  /**
   * Checks if the grid is visible.
   *
   * @return true if the grid is visible, false otherwise.
   */
  public boolean showGrid() {
    return this.showGrid;
  }

  /**
   * Sets the visibility of the grid.
   *
   * @param showGrid true to show the grid, false to hide it.
   */
  public void setShowGrid(boolean showGrid) {
    this.showGrid = showGrid;
  }

  /**
   * Checks if clamping to the map is enabled.
   *
   * @return true if clamping to the map is enabled, false otherwise.
   */
  public boolean clampToMap() {
    return this.clampToMap;
  }

  /**
   * Checks if snapping to pixels is enabled.
   *
   * @return true if snapping to pixels is enabled, false otherwise.
   */
  public boolean snapToPixels() {
    return this.snapToPixels;
  }

  /**
   * Checks if snapping to the grid is enabled.
   *
   * @return true if snapping to the grid is enabled, false otherwise.
   */
  public boolean snapToGrid() {
    return this.snapToGrid;
  }

  /**
   * Sets whether to clamp to the map.
   *
   * @param snapMap true to clamp to the map, false otherwise.
   */
  public void setClampToMap(boolean snapMap) {
    this.clampToMap = snapMap;
  }

  /**
   * Sets whether to snap to pixels.
   *
   * @param snapPixels true to snap to pixels, false otherwise.
   */
  public void setSnapToPixels(boolean snapPixels) {
    this.snapToPixels = snapPixels;
  }

  /**
   * Sets whether to snap to the grid.
   *
   * @param snapGrid true to snap to the grid, false otherwise.
   */
  public void setSnapToGrid(boolean snapGrid) {
    this.snapToGrid = snapGrid;
  }

  /**
   * Checks if rendering bounding boxes is enabled.
   *
   * @return true if rendering bounding boxes is enabled, false otherwise.
   */
  public boolean renderBoundingBoxes() {
    return this.renderBoundingBoxes;
  }

  /**
   * Sets whether to render bounding boxes.
   *
   * @param renderBoundingBoxes true to render bounding boxes, false otherwise.
   */
  public void setRenderBoundingBoxes(boolean renderBoundingBoxes) {
    this.renderBoundingBoxes = renderBoundingBoxes;
  }

  /**
   * Checks if rendering custom map objects is enabled.
   *
   * @return true if rendering custom map objects is enabled, false otherwise.
   */
  public boolean renderCustomMapObjects() {
    return this.renderCustomMapObjects;
  }

  /**
   * Sets whether to render custom map objects.
   *
   * @param renderCustomMapObjects true to render custom map objects, false otherwise.
   */
  public void setRenderCustomMapObjects(boolean renderCustomMapObjects) {
    this.renderCustomMapObjects = renderCustomMapObjects;
  }

  /**
   * Checks if rendering map IDs is enabled.
   *
   * @return true if rendering map IDs is enabled, false otherwise.
   */
  public boolean renderMapIds() {
    return this.renderMapIds;
  }

  /**
   * Sets whether to render map IDs.
   *
   * @param renderIds true to render map IDs, false otherwise.
   */
  public void setRenderMapIds(boolean renderIds) {
    this.renderMapIds = renderIds;
  }

  /**
   * Gets the last game file.
   *
   * @return the last game file.
   */
  public String getLastGameFile() {
    return this.lastGameFile;
  }

  /**
   * Sets the last game file.
   *
   * @param lastGameFile the new last game file.
   */
  public void setLastGameFile(String lastGameFile) {
    this.lastGameFile = lastGameFile;
  }

  /**
   * Gets the list of last opened files.
   *
   * @return an array of the last opened files.
   */
  public String[] getLastOpenedFiles() {
    return this.lastOpenedFiles;
  }

  /**
   * Clears the list of last opened files.
   */
  public void clearOpenedFiles() {
    this.lastOpenedFiles = new String[10];
  }

  /**
   * Sets the list of last opened files.
   *
   * @param lastOpenedFiles an array of the new last opened files.
   */
  public void setLastOpenedFiles(String[] lastOpenedFiles) {
    this.lastOpenedFiles = lastOpenedFiles;
  }

  /**
   * Checks if file compression is enabled.
   *
   * @return true if file compression is enabled, false otherwise.
   */
  public boolean compressFile() {
    return compressFile;
  }

  /**
   * Sets whether to enable file compression.
   *
   * @param compressFile true to enable file compression, false otherwise.
   */
  public void setCompressFile(boolean compressFile) {
    this.compressFile = compressFile;
  }

  /**
   * Gets the width of the grid lines.
   *
   * @return the width of the grid lines.
   */
  public float getGridLineWidth() {
    return this.gridLineWidth;
  }

  /**
   * Gets the color of the grid lines.
   *
   * @return the color of the grid lines.
   */
  public Color getGridColor() {
    return ColorHelper.decode(this.gridColor);
  }

  /**
   * Gets the snap division.
   *
   * @return the snap division.
   */
  public int getSnapDivision() {
    return this.snapDivision;
  }

  /**
   * Sets the width of the grid lines.
   *
   * @param gridLineWidth the new width of the grid lines.
   */
  public void setGridLineWidth(float gridLineWidth) {
    this.gridLineWidth = gridLineWidth;
  }

  /**
   * Sets the color of the grid lines.
   *
   * @param gridColor the new color of the grid lines.
   */
  public void setGridColor(String gridColor) {
    this.gridColor = gridColor;
  }

  /**
   * Sets the snap division.
   *
   * @param snapDivision the new snap division.
   */
  public void setSnapDivision(int snapDivision) {
    this.snapDivision = snapDivision;
  }

  /**
   * Checks if map synchronization is enabled.
   *
   * @return true if map synchronization is enabled, false otherwise.
   */
  public boolean syncMaps() {
    return syncMaps;
  }

  /**
   * Sets whether to enable map synchronization.
   *
   * @param syncMaps true to enable map synchronization, false otherwise.
   */
  public void setSyncMaps(boolean syncMaps) {
    this.syncMaps = syncMaps;
  }

  /**
   * Gets the position of the main splitter.
   *
   * @return the position of the main splitter.
   */
  public int getMainSplitterPosition() {
    return mainSplitter;
  }

  /**
   * Sets the position of the main splitter.
   *
   * @param mainSplitter the new position of the main splitter.
   */
  public void setMainSplitter(int mainSplitter) {
    this.mainSplitter = mainSplitter;
  }

  /**
   * Gets the width of the window.
   *
   * @return the width of the window.
   */
  public int getWidth() {
    return width;
  }

  /**
   * Sets the width of the window.
   *
   * @param width the new width of the window.
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /**
   * Gets the height of the window.
   *
   * @return the height of the window.
   */
  public int getHeight() {
    return height;
  }

  /**
   * Sets the height of the window.
   *
   * @param height the new height of the window.
   */
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Gets the position of the selection edit splitter.
   *
   * @return the position of the selection edit splitter.
   */
  public int getSelectionEditSplitter() {
    return selectionEditSplitter;
  }

  /**
   * Sets the position of the selection edit splitter.
   *
   * @param selectionEditSplitter the new position of the selection edit splitter.
   */
  public void setSelectionEditSplitter(int selectionEditSplitter) {
    this.selectionEditSplitter = selectionEditSplitter;
  }

  /**
   * Gets the position of the map panel splitter.
   *
   * @return the position of the map panel splitter.
   */
  public int getMapPanelSplitter() {
    return mapPanelSplitter;
  }

  /**
   * Sets the position of the map panel splitter.
   *
   * @param mapPanelSplitter the new position of the map panel splitter.
   */
  public void setMapPanelSplitter(int mapPanelSplitter) {
    this.mapPanelSplitter = mapPanelSplitter;
  }

  /**
   * Gets the position of the bottom splitter.
   *
   * @return the position of the bottom splitter.
   */
  public int getBottomSplitter() {
    return bottomSplitter;
  }

  /**
   * Sets the position of the bottom splitter.
   *
   * @param bottomSplitter the new position of the bottom splitter.
   */
  public void setBottomSplitter(int bottomSplitter) {
    this.bottomSplitter = bottomSplitter;
  }

  /**
   * Gets the position of the assets splitter.
   *
   * @return the position of the assets splitter.
   */
  public int getAssetsSplitter() {
    return assetsSplitter;
  }

  /**
   * Sets the position of the assets splitter.
   *
   * @param assetsSplitter the new position of the assets splitter.
   */
  public void setAssetsSplitter(int assetsSplitter) {
    this.assetsSplitter = assetsSplitter;
  }

  /**
   * Gets the frame state.
   *
   * @return the frame state.
   */
  public int getFrameState() {
    return frameState;
  }

  /**
   * Sets the frame state.
   *
   * @param frameState the new frame state.
   */
  public void setFrameState(int frameState) {
    this.frameState = frameState;
  }

  /**
   * Checks if rendering names is enabled.
   *
   * @return true if rendering names is enabled, false otherwise.
   */
  public boolean renderNames() {
    return renderNames;
  }

  /**
   * Sets whether to render names.
   *
   * @param renderNames true to render names, false otherwise.
   */
  public void setRenderNames(boolean renderNames) {
    this.renderNames = renderNames;
  }

  /**
   * Gets the UI scale.
   *
   * @return the UI scale.
   */
  public float getUiScale() {
    return uiScale;
  }

  /**
   * Sets the UI scale.
   *
   * @param uiScale the new UI scale.
   */
  public void setUiScale(float uiScale) {
    this.uiScale = Math.clamp(uiScale, UI_SCALE_MIN, UI_SCALE_MAX);
  }

  /**
   * Gets the current theme.
   *
   * @return the current theme.
   */
  public Theme getTheme() {
    return theme;
  }

  /**
   * Sets the theme.
   *
   * @param theme the new theme.
   */
  public void setTheme(Theme theme) {
    this.theme = theme;
  }
}
