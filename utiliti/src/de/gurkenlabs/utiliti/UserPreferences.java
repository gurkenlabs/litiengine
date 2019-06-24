package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.gurkenlabs.litiengine.configuration.ConfigurationGroup;
import de.gurkenlabs.litiengine.configuration.ConfigurationGroupInfo;
import de.gurkenlabs.litiengine.util.ColorHelper;

@ConfigurationGroupInfo(prefix = "user_")
public class UserPreferences extends ConfigurationGroup {
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
  private double snapDivision;

  private String lastGameFile;
  private String[] lastOpenedFiles;
  private float uiScale;

  public UserPreferences() {
    this.zoom = 1.0f;
    this.showGrid = true;
    this.clampToMap = true;
    this.snapToPixels = true;
    this.snapToGrid = true;
    this.renderBoundingBoxes = true;
    this.renderNames = true;
    this.lastOpenedFiles = new String[10];
    this.compressFile = false;
    this.gridLineWidth = 1.0f;
    this.gridColor = ColorHelper.encode(Style.COLOR_DEFAULT_GRID);
    this.snapDivision = 1.0;
    this.setUiScale(1.0f);
  }

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
    newFiles.add(0, str);
    newFiles.removeAll(Collections.singleton(null));
    // clear array
    this.lastOpenedFiles = new String[10];

    // fill array
    for (int i = 0; i < newFiles.size(); i++) {
      this.lastOpenedFiles[i] = newFiles.get(i);
    }

  }

  public float getZoom() {
    return this.zoom;
  }

  public void setZoom(float zoom) {
    this.zoom = zoom;
  }

  public boolean showGrid() {
    return this.showGrid;
  }

  public void setShowGrid(boolean showGrid) {
    this.showGrid = showGrid;
  }

  public boolean clampToMap() {
    return this.clampToMap;
  }

  public boolean snapToPixels() {
    return this.snapToPixels;
  }

  public boolean snapToGrid() {
    return this.snapToGrid;
  }

  public void setClampToMap(boolean snapMap) {
    this.clampToMap = snapMap;
  }

  public void setSnapToPixels(boolean snapPixels) {
    this.snapToPixels = snapPixels;
  }

  public void setSnapToGrid(boolean snapGrid) {
    this.snapToGrid = snapGrid;
  }

  public boolean renderBoundingBoxes() {
    return this.renderBoundingBoxes;
  }

  public void setRenderBoundingBoxes(boolean renderBoundingBoxes) {
    this.renderBoundingBoxes = renderBoundingBoxes;
  }

  public boolean renderCustomMapObjects() {
    return this.renderCustomMapObjects;
  }

  public void setRenderCustomMapObjects(boolean renderCustomMapObjects) {
    this.renderCustomMapObjects = renderCustomMapObjects;
  }

  public boolean renderMapIds() {
    return this.renderMapIds;
  }

  public void setRenderMapIds(boolean renderIds) {
    this.renderMapIds = renderIds;
  }

  public String getLastGameFile() {
    return this.lastGameFile;
  }

  public void setLastGameFile(String lastGameFile) {
    this.lastGameFile = lastGameFile;
  }

  public String[] getLastOpenedFiles() {
    return this.lastOpenedFiles;
  }

  public void clearOpenedFiles() {
    this.lastOpenedFiles = new String[10];
  }

  public void setLastOpenedFiles(String[] lastOpenedFiles) {
    this.lastOpenedFiles = lastOpenedFiles;
  }

  public boolean compressFile() {
    return compressFile;
  }

  public void setCompressFile(boolean compressFile) {
    this.compressFile = compressFile;
  }

  public float getGridLineWidth() {
    return this.gridLineWidth;
  }

  public Color getGridColor() {
    return ColorHelper.decode(this.gridColor);
  }
  
  public double getSnapDivision() {
    return this.snapDivision;
  }

  public void setGridLineWidth(float gridLineWidth) {
    this.gridLineWidth = gridLineWidth;
  }

  public void setGridColor(String gridColor) {
    this.gridColor = gridColor;
  }
  
  public void setSnapDivision(double snapDivision) {
    this.snapDivision = snapDivision;
  }

  public boolean syncMaps() {
    return syncMaps;
  }

  public void setSyncMaps(boolean syncMaps) {
    this.syncMaps = syncMaps;
  }

  public int getMainSplitterPosition() {
    return mainSplitter;
  }

  public void setMainSplitter(int mainSplitter) {
    this.mainSplitter = mainSplitter;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getSelectionEditSplitter() {
    return selectionEditSplitter;
  }

  public void setSelectionEditSplitter(int selectionEditSplitter) {
    this.selectionEditSplitter = selectionEditSplitter;
  }

  public int getMapPanelSplitter() {
    return mapPanelSplitter;
  }

  public void setMapPanelSplitter(int mapPanelSplitter) {
    this.mapPanelSplitter = mapPanelSplitter;
  }

  public int getBottomSplitter() {
    return bottomSplitter;
  }

  public void setBottomSplitter(int bottomSplitter) {
    this.bottomSplitter = bottomSplitter;
  }

  public int getAssetsSplitter() {
    return assetsSplitter;
  }

  public void setAssetsSplitter(int assetsSplitter) {
    this.assetsSplitter = assetsSplitter;
  }

  public int getFrameState() {
    return frameState;
  }

  public void setFrameState(int frameState) {
    this.frameState = frameState;
  }

  public boolean renderNames() {
    return renderNames;
  }

  public void setRenderNames(boolean renderNames) {
    this.renderNames = renderNames;
  }

  public float getUiScale() {
    return uiScale;
  }

  public void setUiScale(float uiScale) {
    this.uiScale = uiScale;
  }
}